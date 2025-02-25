package org.example.autopark.GPS;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointsGPSRepository extends JpaRepository<PointGPS, Long> {
}
