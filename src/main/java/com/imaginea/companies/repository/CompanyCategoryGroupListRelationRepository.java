package com.imaginea.companies.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.imaginea.companies.model.CompanyCategoryGroupListRelation;
import com.imaginea.companies.model.CompanyCategoryListRelation;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyCategoryGroupListRelationRepository extends
    ArangoRepository<CompanyCategoryGroupListRelation, String> {

  Optional<CompanyCategoryListRelation> findByFromNameAndToName(String fromName, String toName);

}
