package com.imaginea.companies.model;

import com.arangodb.springframework.annotation.Edge;
import com.arangodb.springframework.annotation.From;
import com.arangodb.springframework.annotation.To;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Edge
@Data
public class CompanyCategoryListRelation {

  @Id
  private String id;

  @From
  private Company from;

  @To
  private CategoryList to;

  public CompanyCategoryListRelation(Company from, CategoryList to) {
    this.from = from;
    this.to = to;
  }

}
