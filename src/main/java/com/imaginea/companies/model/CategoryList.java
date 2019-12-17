package com.imaginea.companies.model;

import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.HashIndex;
import com.arangodb.springframework.annotation.Relations;
import com.arangodb.springframework.annotation.Relations.Direction;
import java.util.Collection;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;

@Document("categorylist")
@Data
@HashIndex(fields = {"name"}, unique = true)
public class CategoryList {

  @Id
  private String id;

  @NonNull
  private String name;

  @Relations(edges = CompanyCategoryListRelation.class, lazy = true, direction = Direction.OUTBOUND)
  private Collection<CategoryList> categoryLists;

}
