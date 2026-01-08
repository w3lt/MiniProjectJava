package com.aletheia.miniproject.repos;

import com.aletheia.miniproject.core.entities.Association;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAssociationRepository extends JpaRepository<Association, Long> {}
