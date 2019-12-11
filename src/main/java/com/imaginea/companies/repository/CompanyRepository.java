package com.imaginea.companies.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.imaginea.companies.model.Company;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends ArangoRepository<Company, String> {

  Optional<Company> findByName(String name);

}
