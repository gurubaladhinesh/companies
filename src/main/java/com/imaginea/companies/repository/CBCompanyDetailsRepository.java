package com.imaginea.companies.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.imaginea.companies.model.CBCompanyDetails;

public interface CBCompanyDetailsRepository extends ArangoRepository<CBCompanyDetails, String> {

}
