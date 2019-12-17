package com.imaginea.companies.repository;


import com.arangodb.springframework.repository.ArangoRepository;
import com.imaginea.companies.model.CategoryGroupList;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryGroupListRepository extends ArangoRepository<CategoryGroupList, String> {

  Optional<CategoryGroupList> findByName(String name);

}