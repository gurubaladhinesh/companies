package com.imaginea.companies.model;

import com.arangodb.springframework.annotation.Edge;
import com.arangodb.springframework.annotation.From;
import com.arangodb.springframework.annotation.To;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Edge
@Data
public class CompanyCategoryGroupListRelation {

  @Id
  private String id;

  @From
  private Company from;

  @To
  private CategoryGroupList to;

  public CompanyCategoryGroupListRelation(Company from, CategoryGroupList to) {
    this.from = from;
    this.to = to;
  }

}