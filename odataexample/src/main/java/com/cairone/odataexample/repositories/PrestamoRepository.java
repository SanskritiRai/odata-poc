package com.cairone.odataexample.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.odataexample.entities.PrestamoEntity;

public interface PrestamoRepository extends JpaRepository<PrestamoEntity, Integer>, QueryDslPredicateExecutor<PrestamoEntity> {

}
