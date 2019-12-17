package com.imaginea.companies.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.imaginea.companies.model.CompanyCBCompanyDetailsRelation;

public interface CompanyCBCompanyDetailsRelationRepository extends
    ArangoRepository<CompanyCBCompanyDetailsRelation, String> {

}
