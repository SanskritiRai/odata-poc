package com.cairone.odataexample.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.cairone.odataexample.entities.PrestamoCuotaEntity;
import com.cairone.odataexample.entities.PrestamoCuotaPKEntity;

public interface PrestamoCuotaRepository extends JpaRepository<PrestamoCuotaEntity, PrestamoCuotaPKEntity>, QueryDslPredicateExecutor<PrestamoCuotaEntity> {

}
