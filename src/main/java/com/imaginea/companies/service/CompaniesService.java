package com.imaginea.companies.service;

import com.arangodb.springframework.core.ArangoOperations;
import com.imaginea.companies.exception.CompaniesException;
import com.imaginea.companies.model.CategoryList;
import com.imaginea.companies.model.Company;
import com.imaginea.companies.model.CompanyCategoryListRelation;
import com.imaginea.companies.repository.CategoryListRepository;
import com.imaginea.companies.repository.CompanyCategoryListRelationRepository;
import com.imaginea.companies.repository.CompanyRepository;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

@Service
@ComponentScan("com.arangodb.springframework.core")
@DependsOn({"companyCategoryListRelationRepository", "categoryListRepository", "companyRepository"})
@Slf4j
public class CompaniesService {

  private ArangoOperations operations;

  private CompanyRepository companyRepository;

  private CategoryListRepository categoryListRepository;

  private CompanyCategoryListRelationRepository companyCategoryListRelationRepository;

  private static Workbook cbWorkbook;

  @Autowired
  public CompaniesService(ArangoOperations operations, CompanyRepository companyRepository,
      CategoryListRepository categoryListRepository,
      CompanyCategoryListRelationRepository companyCategoryListRelationRepository) {
    this.operations = operations;
    this.companyRepository = companyRepository;
    this.categoryListRepository = categoryListRepository;
    this.companyCategoryListRelationRepository = companyCategoryListRelationRepository;
  }

  @PostConstruct
  public void populateCompanies() throws CompaniesException {
    try {
      Instant start = Instant.now();
      operations.dropDatabase();
      operations.collection(Company.class);
      operations.collection(CategoryList.class);
      operations.collection(CompanyCategoryListRelation.class);

      //String filePath = "/home/gurubaladhinesh/Downloads/Crunchbase_Flatfile_v-lighter_truncated.xlsx";
      String filePath = "/home/gurubaladhinesh/Downloads/Crunchbase_Flatfile_v-lighter.xlsx";
      //String filePath = "/home/gurubaladhinesh/Documents/mini_crunchbase.xlsx";
      File cbCompanyDetailsFile = new File(filePath);
      cbWorkbook = WorkbookFactory.create(cbCompanyDetailsFile);
      Instant finish = Instant.now();
      log.info("Time take by Apache POI to load crunchbase xlsx file is {} seconds",
          Duration.between(start, finish).toSeconds());
      start = Instant.now();

      Sheet sheet = cbWorkbook.getSheet("Funded Companies");
      int companyNameColumnIndex = CellReference.convertColStringToIndex("A");
      int domainColumnIndex = CellReference.convertColStringToIndex("B");
      int categoryListColumnIndex = CellReference.convertColStringToIndex("J");

      for (Row row : sheet) {
        if (row.getRowNum() == 0) {
          continue;
        }

        //Company Name
        Cell cell = row.getCell(companyNameColumnIndex);
        String companyName = (String) getCellValue(cell);

        //Domain
        cell = row.getCell(domainColumnIndex);
        String domain = (String) getCellValue(cell);

        Company company = new Company(companyName, domain);
        Company finalCompany = company;
        company = companyRepository.findByName(companyName).orElseGet(
            () -> companyRepository.save(finalCompany)
        );

        cell = row.getCell(categoryListColumnIndex);
        String categoryListStr = (String) getCellValue(cell);

        if (!categoryListStr.isBlank()) {

          String[] categoryListArr = categoryListStr.split("\\|");
          for (String category : categoryListArr) {
            category = category.trim().toLowerCase();

            //CategoryList
            CategoryList categoryList = new CategoryList(category);
            CategoryList finalCategoryList = categoryList;
            categoryList = categoryListRepository.findByName(category)
                .orElseGet(
                    () -> categoryListRepository.save(finalCategoryList)

                );
            // Company-CategoryList relation
            CompanyCategoryListRelation companyCategoryListRelation = new CompanyCategoryListRelation(
                company, categoryList);
            CompanyCategoryListRelation finalCompanyCategoryListRelation = companyCategoryListRelation;
            companyCategoryListRelation = companyCategoryListRelationRepository
                .findByFromNameAndToName(company.getName(), categoryList.getName())
                .orElseGet(() -> companyCategoryListRelationRepository
                    .save(finalCompanyCategoryListRelation));
          }
        }
      }
      finish = Instant.now();
      log.info("Time take to add rows from xlsx file to graph db is {} seconds",
          Duration.between(start, finish).toSeconds());

    } catch (InvalidDataAccessApiUsageException |
        IOException e) {
      //Arango error code 1205 - Already exists
      if (e.getMessage().contains("1205")) {
        throw new CompaniesException("Node already exists");
      }
      throw new CompaniesException(e);
    }
  }

  private Object getCellValue(Cell cell) {
    return cell != null ? getCellValue(cell, cell.getCellType()) : "";
  }

  private Object getCellValue(Cell cell, CellType cellType) {
    Object cellValue;
    switch (cellType) {
      case BOOLEAN:
        cellValue = cell.getBooleanCellValue();
        break;
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          cellValue = cell.getDateCellValue();
        } else {
          cellValue = cell.getNumericCellValue();
        }
        break;
      case STRING:
        cellValue = cell.getStringCellValue();
        break;
      case FORMULA:
        cellValue = getCellValue(cell, cell.getCachedFormulaResultType());
        break;
      default:
        cellValue = "";
        break;
    }
    return cellValue;
  }

}
