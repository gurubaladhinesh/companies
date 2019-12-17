package com.imaginea.companies.service;

import com.arangodb.springframework.core.ArangoOperations;
import com.imaginea.companies.exception.CompaniesException;
import com.imaginea.companies.model.CBCompanyDetails;
import com.imaginea.companies.model.CategoryGroupList;
import com.imaginea.companies.model.CategoryList;
import com.imaginea.companies.model.Company;
import com.imaginea.companies.model.CompanyCBCompanyDetailsRelation;
import com.imaginea.companies.model.CompanyCategoryGroupListRelation;
import com.imaginea.companies.model.CompanyCategoryListRelation;
import com.imaginea.companies.repository.CBCompanyDetailsRepository;
import com.imaginea.companies.repository.CategoryGroupListRepository;
import com.imaginea.companies.repository.CategoryListRepository;
import com.imaginea.companies.repository.CompanyCBCompanyDetailsRelationRepository;
import com.imaginea.companies.repository.CompanyCategoryGroupListRelationRepository;
import com.imaginea.companies.repository.CompanyCategoryListRelationRepository;
import com.imaginea.companies.repository.CompanyRepository;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
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
  private CategoryGroupListRepository categoryGroupListRepository;
  private CompanyCategoryListRelationRepository companyCategoryListRelationRepository;
  private CompanyCategoryGroupListRelationRepository companyCategoryGroupListRelationRepository;
  private CBCompanyDetailsRepository cbCompanyDetailsRepository;
  private CompanyCBCompanyDetailsRelationRepository companyCBCompanyDetailsRelationRepository;

  private static Workbook cbWorkbook;

  @Autowired
  public CompaniesService(ArangoOperations operations, CompanyRepository companyRepository,
      CategoryListRepository categoryListRepository,
      CompanyCategoryListRelationRepository companyCategoryListRelationRepository,
      CategoryGroupListRepository categoryGroupListRepository,
      CompanyCategoryGroupListRelationRepository companyCategoryGroupListRelationRepository,
      CBCompanyDetailsRepository cbCompanyDetailsRepository,
      CompanyCBCompanyDetailsRelationRepository companyCBCompanyDetailsRelationRepository
  ) {
    this.operations = operations;
    this.companyRepository = companyRepository;
    this.categoryListRepository = categoryListRepository;
    this.categoryGroupListRepository = categoryGroupListRepository;
    this.companyCategoryListRelationRepository = companyCategoryListRelationRepository;
    this.companyCategoryGroupListRelationRepository = companyCategoryGroupListRelationRepository;
    this.cbCompanyDetailsRepository = cbCompanyDetailsRepository;
    this.companyCBCompanyDetailsRelationRepository = companyCBCompanyDetailsRelationRepository;
  }

  @PostConstruct
  public void populateCompanies() throws CompaniesException {
    try {
      Instant start = Instant.now();
      operations.dropDatabase();
      operations.collection(Company.class);
      operations.collection(CategoryList.class);
      operations.collection(CategoryGroupList.class);
      operations.collection(CBCompanyDetails.class);
      operations.collection(CompanyCategoryListRelation.class);
      operations.collection(CompanyCategoryGroupListRelation.class);
      operations.collection(CompanyCBCompanyDetailsRelation.class);

      //String filePath = "/home/gurubaladhinesh/Downloads/Crunchbase_Flatfile_v-lighter_truncated.xlsx";
      String filePath = "/home/gurubaladhinesh/Downloads/Crunchbase_Flatfile_v-lighter_truncated_10k.xlsx";
      //String filePath = "/home/gurubaladhinesh/Downloads/Crunchbase_Flatfile_v-lighter.xlsx";
      //String filePath = "/home/gurubaladhinesh/Downloads/cb_check_1.xlsx";

      File cbCompanyDetailsFile = new File(filePath);
      cbWorkbook = WorkbookFactory.create(cbCompanyDetailsFile);
      Instant finish = Instant.now();
      log.info("Time take by Apache POI to load crunchbase xlsx file is {} seconds",
          Duration.between(start, finish).toSeconds());
      start = Instant.now();

      Sheet sheet = cbWorkbook.getSheet("Funded Companies");
      int companyNameColumnIndex = CellReference.convertColStringToIndex("A");
      int domainColumnIndex = CellReference.convertColStringToIndex("B");
      int countryCodeColumnIndex = CellReference.convertColStringToIndex("C");
      int stateCodeColumnIndex = CellReference.convertColStringToIndex("D");
      int regionColumnIndex = CellReference.convertColStringToIndex("E");
      int cityColumnIndex = CellReference.convertColStringToIndex("F");
      int statusColumnIndex = CellReference.convertColStringToIndex("G");
      int shortDescriptionColumnIndex = CellReference.convertColStringToIndex("H");
      int rankColumnIndex = CellReference.convertColStringToIndex("I");
      int categoryListColumnIndex = CellReference.convertColStringToIndex("J");
      int categoryGroupListColumnIndex = CellReference.convertColStringToIndex("K");
      int employeeCountColumnIndex = CellReference.convertColStringToIndex("L");
      int fundingRoundsColumnIndex = CellReference.convertColStringToIndex("M");
      int fundingTotalUSDColumnIndex = CellReference.convertColStringToIndex("N");
      int foundedOnColumnIndex = CellReference.convertColStringToIndex("O");
      int firstFundingOnColumnIndex = CellReference.convertColStringToIndex("P");
      int lastFundingOnColumnIndex = CellReference.convertColStringToIndex("Q");
      int closedOnColumnIndex = CellReference.convertColStringToIndex("R");
      int emailColumnIndex = CellReference.convertColStringToIndex("S");
      int phoneColumnIndex = CellReference.convertColStringToIndex("T");
      int facebookUrlColumnIndex = CellReference.convertColStringToIndex("U");
      int cbUrlColumnIndex = CellReference.convertColStringToIndex("V");
      int twitterUrlColumnIndex = CellReference.convertColStringToIndex("W");
      int uuidColumnIndex = CellReference.convertColStringToIndex("X");

      int rowLimit = 10000;

      Set<String> companyNameSet = new HashSet<>();
      Set<String> categoryListSet = new HashSet<>();
      Set<String> categoryGroupListSet = new HashSet<>();
      Map<String, String> companyCategoryListRelationMap = new HashMap<>();
      Map<String, String> companyCategoryGroupListRelationMap = new HashMap<>();
      int companyCount = 0;
      int companyDetailsCount = 0;

      for (Row row : sheet) {
        if (row.getRowNum() == 0) {
          continue;
        }

        if (row.getRowNum() > rowLimit) {
          break;
        }

        //Company Name
        String companyName = getStringCellValue(row.getCell(companyNameColumnIndex));

        if (!companyName.isBlank()) {
          //Domain
          String domain = getStringCellValue(row.getCell(domainColumnIndex));

          Company company;

          if (companyNameSet.contains(companyName)) {
            company = companyRepository.findByName(companyName).get();
          } else {
            companyCount++;
            company = companyRepository.save(new Company(companyName, domain));
            companyNameSet.add(company.getName());
          }

       /*   CompletableFuture<Void> categoryListFuture = CompletableFuture.runAsync(
              () -> {*/

          String categoryListStr = getStringCellValue(row.getCell(categoryListColumnIndex));
          if (!categoryListStr.isBlank()) {
            String[] categoryListArr = categoryListStr.split("\\|");
            for (String category : categoryListArr) {
              category = category.trim().toLowerCase();

              //CategoryList
              CategoryList categoryList;
              if (categoryListSet.contains(category)) {
                categoryList = categoryListRepository.findByName(category).get();
              } else {
                categoryList = categoryListRepository.save(new CategoryList(category));
                categoryListSet.add(category);
              }

              // Company-CategoryList relation

              if (!(companyCategoryListRelationMap.containsKey(company.getName())
                  && companyCategoryListRelationMap.containsValue(categoryList.getName()))) {
                companyCategoryListRelationRepository
                    .save(new CompanyCategoryListRelation(
                        company, categoryList));
                companyCategoryListRelationMap.put(company.getName(), categoryList.getName());
              }
            }
          }
              /*}
          );

          CompletableFuture<Void> categoryGroupListFuture = CompletableFuture.runAsync(
              () -> {*/

          String categoryGroupListStr = getStringCellValue(
              row.getCell(categoryGroupListColumnIndex));
          if (!categoryGroupListStr.isBlank()) {
            String[] categoryGroupListArr = categoryGroupListStr.split("\\|");
            for (String categoryGroup : categoryGroupListArr) {
              categoryGroup = categoryGroup.trim().toLowerCase();

              //CategoryList
              CategoryGroupList categoryGroupList;
              if (categoryGroupListSet.contains(categoryGroup)) {
                categoryGroupList = categoryGroupListRepository.findByName(categoryGroup)
                    .get();
              } else {
                categoryGroupList = categoryGroupListRepository
                    .save(new CategoryGroupList(categoryGroup));
                categoryGroupListSet.add(categoryGroup);
              }

              // Company-CategoryGroupList relation

              if (!(companyCategoryGroupListRelationMap.containsKey(company.getName())
                  && companyCategoryGroupListRelationMap
                  .containsValue(categoryGroupList.getName()))) {
                companyCategoryGroupListRelationRepository
                    .save(new CompanyCategoryGroupListRelation(
                        company, categoryGroupList));
                companyCategoryGroupListRelationMap
                    .put(company.getName(), categoryGroupList.getName());
              }
            }
          }
              /*}
          );

          //CB Company Details
          CompletableFuture<CBCompanyDetails> cbCompanyDetailsFuture = CompletableFuture
              .supplyAsync(
                  () -> {*/
          String countryCode = getStringCellValue(row.getCell(countryCodeColumnIndex));
          String stateCode = getStringCellValue(row.getCell(stateCodeColumnIndex));
          String region = getStringCellValue(row.getCell(regionColumnIndex));
          String city = getStringCellValue(row.getCell(cityColumnIndex));
          String status = getStringCellValue(row.getCell(statusColumnIndex));
          String shortDescription = getStringCellValue(
              row.getCell(shortDescriptionColumnIndex));
          String rank = getStringCellValue(row.getCell(rankColumnIndex));
          String employeeCount = getStringCellValue(
              row.getCell(employeeCountColumnIndex));
          Integer fundingRounds = getIntegerCellValue(row.getCell(fundingRoundsColumnIndex));

          Double fundingTotalUSD = getDoubleCellValue(row.getCell(fundingTotalUSDColumnIndex));

          LocalDate foundedOn = getLocalDateCellValue(row.getCell(foundedOnColumnIndex,
              MissingCellPolicy.RETURN_BLANK_AS_NULL));
          LocalDate firstFundingOn = getLocalDateCellValue(
              row.getCell(firstFundingOnColumnIndex,
                  MissingCellPolicy.RETURN_BLANK_AS_NULL));
          LocalDate lastFundingOn = getLocalDateCellValue(
              row.getCell(lastFundingOnColumnIndex,
                  MissingCellPolicy.RETURN_BLANK_AS_NULL));
          LocalDate closedOn = getLocalDateCellValue(
              row.getCell(closedOnColumnIndex, MissingCellPolicy.RETURN_BLANK_AS_NULL));

          String email = getStringCellValue(row.getCell(emailColumnIndex));
          String phone = getStringCellValue(row.getCell(phoneColumnIndex));
          String facebookUrl = getStringCellValue(row.getCell(facebookUrlColumnIndex));
          String cbUrl = getStringCellValue(row.getCell(cbUrlColumnIndex));
          String twitterUrl = getStringCellValue(row.getCell(twitterUrlColumnIndex));
          String uuid = getStringCellValue(row.getCell(uuidColumnIndex));

          CBCompanyDetails cbCompanyDetails = CBCompanyDetails.builder()
              .countryCode(countryCode).stateCode(stateCode).region(region).city(city)
              .status(status).shortDescription(shortDescription).rank(rank)
              .employeeCount(employeeCount)
              .fundingRounds(fundingRounds).fundingTotalUSD(fundingTotalUSD)
              .foundedOn(foundedOn).firstFundingOn(firstFundingOn)
              .lastFundingOn(lastFundingOn).closedOn(closedOn).email(email).phone(phone)
              .facebookUrl(facebookUrl).cbUrl(cbUrl).twitterUrl(twitterUrl).uuid(uuid)
              .build();

          cbCompanyDetails = cbCompanyDetailsRepository.save(cbCompanyDetails);
          companyCBCompanyDetailsRelationRepository
              .save(new CompanyCBCompanyDetailsRelation(company, cbCompanyDetails));
          companyDetailsCount++;
                    /*return cbCompanyDetails;
                  }
              ).exceptionally(
                  (ex) -> {
                    return null;
                  }

              );



          CompletableFuture
              .allOf(categoryListFuture, categoryGroupListFuture, cbCompanyDetailsFuture);*/

        }
      }
      System.out.println("Company:" + companyCount + ", Company details:" + companyDetailsCount);
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
    return cell != null ? getCellValue(cell, cell.getCellType()) : null;
  }

  private Object getCellValue(Cell cell, CellType cellType) {
    Object cellValue;
    switch (cellType) {
      case BOOLEAN:
        cellValue = cell.getBooleanCellValue();
        break;
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          cellValue = cell.getLocalDateTimeCellValue();
        } else {
          cellValue = cell.getNumericCellValue();
        }
        break;
      case STRING:
        cellValue = cell.getStringCellValue();
        break;
      case FORMULA:
        cellValue = cell.getCachedFormulaResultType();
        break;
      default:
        cellValue = "";
        break;
    }
    return cellValue;
  }

  private LocalDate convertToLocalDate(Date date) {
    return date.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate();
  }

  private String getStringCellValue(Cell cell) {
    String defaultString = "";
    Object cellValue = getCellValue(cell);
    return cell == null || ((String) cellValue).isBlank() ? defaultString
        : (String) cellValue;
  }

  private LocalDate getLocalDateCellValue(Cell cell) {
    if (cell == null) {
      return null;
    }
    LocalDate defaultDate = LocalDate.of(1900, 1, 1);
    Object cellValue = getCellValue(cell);
    return cellValue instanceof Double ? defaultDate
        : ((LocalDateTime) cellValue).toLocalDate();
  }

  private Double getDoubleCellValue(Cell cell) {
    Double defaultDouble = Double.valueOf(0.0);
    Object cellValue = getCellValue(cell);
    return cell == null ? defaultDouble
        : (Double) cellValue;
  }

  private Integer getIntegerCellValue(Cell cell) {
    Integer defaultInteger = Integer.valueOf(0);
    Object cellValue = getCellValue(cell);
    return cell == null ? defaultInteger
        : ((Double) cellValue).intValue();
  }


}
