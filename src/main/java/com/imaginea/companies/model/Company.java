package com.imaginea.companies.model;

import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.HashIndex;
import com.arangodb.springframework.annotation.Relations;
import com.arangodb.springframework.annotation.Relations.Direction;
import java.util.Collection;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Document("company")
@Data
@HashIndex(fields = {"name"}, unique = true)
public class Company {

  @Id
  private String id;

  private String name;

  private String domain;

  @Relations(edges = CompanyCategoryListRelation.class, lazy = true, direction = Direction.OUTBOUND)
  private Collection<CategoryList> categoryLists;

  public Company(String name, String domain) {
    this.name = name;
    this.domain = domain;
  }
}
