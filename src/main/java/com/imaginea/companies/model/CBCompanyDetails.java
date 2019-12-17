package com.imaginea.companies.model;

import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.HashIndex;
import com.arangodb.springframework.annotation.Relations;
import com.arangodb.springframework.annotation.Relations.Direction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Document("cb_company_details")
@Data
@Builder
public class CBCompanyDetails {

  @Id
  private String id;

  private String countryCode;

  private String stateCode;

  private String region;

  private String city;

  private String status;

  private String shortDescription;

  private String rank;

  private String employeeCount;

  private Integer fundingRounds;

  private Double fundingTotalUSD;

  private LocalDate foundedOn;

  private LocalDate firstFundingOn;

  private LocalDate lastFundingOn;

  private LocalDate closedOn;

  private String email;

  private String phone;

  private String facebookUrl;

  private String cbUrl;

  private String twitterUrl;

  private String uuid;

  @Relations(edges = CompanyCBCompanyDetailsRelation.class, lazy = true, direction = Direction.OUTBOUND)
  private Collection<CBCompanyDetails> cbCompanyDetails;


}
