package com.imaginea.companies.util;

import com.arangodb.springframework.core.ArangoOperations;
import com.imaginea.companies.exception.CompaniesException;
import com.imaginea.companies.model.CategoryList;
import com.imaginea.companies.model.Company;
import com.imaginea.companies.model.CompanyCategoryListRelation;
import com.imaginea.companies.repository.CategoryListRepository;
import com.imaginea.companies.repository.CompanyCategoryListRelationRepository;
import com.imaginea.companies.repository.CompanyRepository;
import java.io.File;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.InvalidDataAccessApiUsageException;

@ComponentScan("com.imaginea.companies")
public class CompaniesRunner implements CommandLineRunner {

  private ArangoOperations operations;

  private CompanyRepository companyRepository;

  private CategoryListRepository categoryListRepository;

  private CompanyCategoryListRelationRepository companyCategoryListRelationRepository;

  private static Workbook cbWorkbook;

  @Autowired
  public CompaniesRunner(ArangoOperations operations, CompanyRepository companyRepository,
      CategoryListRepository categoryListRepository,
      CompanyCategoryListRelationRepository companyCategoryListRelationRepository) {
    this.operations = operations;
    this.companyRepository = companyRepository;
    this.categoryListRepository = categoryListRepository;
    this.companyCategoryListRelationRepository = companyCategoryListRelationRepository;
  }

  @Override
  public void run(String... args) throws Exception {

    try {
      operations.dropDatabase();
      operations.collection(Company.class);
      operations.collection(CategoryList.class);
      operations.collection(CompanyCategoryListRelation.class);

      String filePath = "/home/gurubaladhinesh/Documents/mini_crunchbase.xlsx"; //"/home/gurubaladhinesh/Downloads/Crunchbase_Flatfile_v-lighter.xlsx"
      File cbCompanyDetailsFile = new File(
          filePath);
      cbWorkbook = WorkbookFactory.create(cbCompanyDetailsFile);

      Sheet sheet = cbWorkbook.getSheetAt(0);

      for (Row row : sheet) {
        if (row.getRowNum() == 0) {
          continue;
        }

        //Company Name
        Cell cell = row.getCell(0);
        String companyName = (String) getCellValue(cell);

        //Domain
        cell = row.getCell(1);
        String domain = (String) getCellValue(cell);
        System.out.println(domain);

        //companyRepository.save(new Company(companyName, domain));
        System.out.println("Company::"+companyName);
        Company company = companyRepository.findByName(companyName).orElse(
            companyRepository.save(new Company(companyName, domain))
        );

        //Category List
        cell = row.getCell(8);
        String categoryListStr = (String) getCellValue(cell);
        String[] categoryListArr = categoryListStr.split("\\|");
        for (String category : categoryListArr
        ) {
          category = category.trim().toLowerCase();
          System.out.println("CategoryList::"+category);
          Optional<CategoryList> categoryListOptional = categoryListRepository.findByName(category);
          System.out.println(categoryListOptional.isPresent());

          String finalCategory = category;
          CategoryList categoryListObj = categoryListOptional
              .orElseGet(() -> categoryListRepository.save(new CategoryList(finalCategory)));

          System.out.println("Company - CategoryList relation::"+company.getName()+","+categoryListObj.getName());
          CompanyCategoryListRelation companyCategoryListRelation = companyCategoryListRelationRepository
              .findByFromNameAndToName(company.getName(), categoryListObj.getName())
              .orElse(companyCategoryListRelationRepository.save(new CompanyCategoryListRelation(
                  company, categoryListObj)));

        }
      }
    } catch (InvalidDataAccessApiUsageException e) {
      //Arango error code 1205 - Already exists
      if (e.getMessage().contains("1205")) {
        throw new CompaniesException("Node already exists");
      }
      throw e;
    }



    /*String[] companiesArray = {"abc1", "def2", "ghi3"};
    System.out.println("Hello populateDb");
    for (String companyStr : companiesArray) {
      Company company = new Company("abc");
      company.setName(companyStr);
      companyRepository.save(company);
    }*/

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
