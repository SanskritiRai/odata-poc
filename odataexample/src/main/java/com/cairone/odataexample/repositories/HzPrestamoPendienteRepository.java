package com.cairone.odataexample.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.odataexample.entities.HzPrestamoPendienteEntity;

public interface HzPrestamoPendienteRepository extends JpaRepository<HzPrestamoPendienteEntity, String>, QueryDslPredicateExecutor<HzPrestamoPendienteEntity> {

}
