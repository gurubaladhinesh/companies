package com.imaginea.companies.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.imaginea.companies.model.CompanyCategoryListRelation;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyCategoryListRelationRepository extends ArangoRepository<CompanyCategoryListRelation, String> {

  Optional<CompanyCategoryListRelation> findByFromNameAndToName(String fromName, String toName);

  //Optional<CompanyCategoryListRelation> findByToName(String name);

}
