package com.imaginea.companies.model;

import com.arangodb.springframework.annotation.Edge;
import com.arangodb.springframework.annotation.From;
import com.arangodb.springframework.annotation.To;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Edge
@Data
public class CompanyCBCompanyDetailsRelation {

  @Id
  private String id;

  @From
  private Company from;

  @To
  private CBCompanyDetails to;

  public CompanyCBCompanyDetailsRelation(Company from, CBCompanyDetails to) {
    this.from = from;
    this.to = to;
  }

}
