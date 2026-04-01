package com.tneaguider.backend.repository;

import com.tneaguider.backend.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {

    @Query(value = """
        SELECT 
            c.id,
            c.name,
            b.name as branch_name,
            cat.name as category_name,
            d.name as district_name,
            cd.cutoff,
            cd.fees,
            cd.placement_rate,
            c.tier
        FROM colleges c
        JOIN cutoff_data cd ON c.id = cd.college_id
        JOIN branches b ON cd.branch_id = b.id
        JOIN categories cat ON cd.category_id = cat.id
        JOIN districts d ON c.district_id = d.id
        """, nativeQuery = true)
    List<Object[]> findAllWithDetails();

    @Query(value = """
        SELECT 
            c.id,
            c.name,
            b.name as branch_name,
            cat.name as category_name,
            d.name as district_name,
            cd.cutoff,
            cd.fees,
            cd.placement_rate,
            c.tier
        FROM colleges c
        JOIN cutoff_data cd ON c.id = cd.college_id
        JOIN branches b ON cd.branch_id = b.id
        JOIN categories cat ON cd.category_id = cat.id
        JOIN districts d ON c.district_id = d.id
        WHERE cat.name = :category
        """, nativeQuery = true)
    List<Object[]> findAllWithDetailsByCategory(@Param("category") String category);

    @Query(value = """
        SELECT 
            c.id,
            c.name,
            b.name as branch_name,
            cat.name as category_name,
            d.name as district_name,
            cd.cutoff,
            cd.fees,
            cd.placement_rate,
            c.tier
        FROM cutoff_data cd
        JOIN colleges c ON c.id = cd.college_id
        JOIN branches b ON cd.branch_id = b.id
        JOIN categories cat ON cd.category_id = cat.id
        JOIN districts d ON c.district_id = d.id
        WHERE cd.cutoff <= :cutoff
        """, nativeQuery = true)
    List<Object[]> findAllWithDetailsByCutoff(@Param("cutoff") Double cutoff);

    @Query(value = """
        SELECT 
            c.id,
            c.name,
            b.name as branch_name,
            cat.name as category_name,
            d.name as district_name,
            cd.cutoff,
            cd.fees,
            cd.placement_rate,
            c.tier
        FROM cutoff_data cd
        JOIN colleges c ON c.id = cd.college_id
        JOIN branches b ON cd.branch_id = b.id
        JOIN categories cat ON cd.category_id = cat.id
        JOIN districts d ON c.district_id = d.id
        WHERE cd.cutoff <= :cutoff AND cat.name = :category
        """, nativeQuery = true)
    List<Object[]> findAllWithDetailsByCutoffAndCategory(@Param("cutoff") Double cutoff, @Param("category") String category);

    @Query(value = """
        SELECT 
            c.id,
            c.name,
            b.name as branch_name,
            cat.name as category_name,
            d.name as district_name,
            cd.cutoff,
            cd.fees,
            cd.placement_rate,
            c.tier
        FROM cutoff_data cd
        JOIN colleges c ON c.id = cd.college_id
        JOIN branches b ON cd.branch_id = b.id
        JOIN categories cat ON cd.category_id = cat.id
        JOIN districts d ON c.district_id = d.id
        WHERE cd.cutoff <= :cutoff
        AND (:district IS NULL OR LOWER(d.name) = LOWER(:district))
        AND (:tier IS NULL OR c.tier = :tier)
        AND (:maxFees IS NULL OR cd.fees <= :maxFees)
        ORDER BY cd.cutoff DESC, cd.placement_rate DESC
        """, nativeQuery = true)
    List<Object[]> findWithFilters(
        @Param("cutoff") Double cutoff,
        @Param("district") String district,
        @Param("tier") String tier,
        @Param("maxFees") Double maxFees
    );

    @Query(value = """
        SELECT 
            c.id,
            c.name,
            b.name as branch_name,
            cat.name as category_name,
            d.name as district_name,
            cd.cutoff,
            cd.fees,
            cd.placement_rate,
            c.tier
        FROM cutoff_data cd
        JOIN colleges c ON c.id = cd.college_id
        JOIN branches b ON cd.branch_id = b.id
        JOIN categories cat ON cd.category_id = cat.id
        JOIN districts d ON c.district_id = d.id
        WHERE cd.cutoff <= :cutoff
        AND cd.cutoff >= :minCutoff
        ORDER BY cd.cutoff DESC, cd.placement_rate DESC
        """, nativeQuery = true)
    List<Object[]> findWithCutoffRange(
        @Param("cutoff") Double cutoff,
        @Param("minCutoff") Double minCutoff
    );
}