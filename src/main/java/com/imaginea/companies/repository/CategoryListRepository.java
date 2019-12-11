package com.imaginea.companies.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.imaginea.companies.model.CategoryList;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryListRepository extends ArangoRepository<CategoryList, String> {

  Optional<CategoryList> findByName(String name);

}
