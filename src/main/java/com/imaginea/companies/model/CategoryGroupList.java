package com.imaginea.companies.model;

import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.HashIndex;
import com.arangodb.springframework.annotation.Relations;
import com.arangodb.springframework.annotation.Relations.Direction;
import java.util.Collection;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;

@Document("categorygrouplist")
@Data
@HashIndex(fields = {"name"}, unique = true)
public class CategoryGroupList {

  @Id
  private String id;

  @NonNull
  private String name;

  @Relations(edges = CompanyCategoryGroupListRelation.class, lazy = true, direction = Direction.OUTBOUND)
  private Collection<CategoryGroupList> categoryGroupLists;

}
