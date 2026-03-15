package com.smartpharma.repository;

import com.smartpharma.entity.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 药品信息数据访问层
 * 继承 JpaRepository 提供单表 CRUD，自定义条件分页查询。
 */
public interface DrugRepository extends JpaRepository<Drug, Long> {

    /** 按名称模糊、类别精确、是否包含停用进行分页查询 */
    @Query("select d from Drug d where (:name is null or d.name like concat('%', :name, '%')) and (:category is null or d.category = :category) and (d.disabled = false or :includeDisabled = true)")
    Page<Drug> findByCondition(@Param("name") String name, @Param("category") String category, @Param("includeDisabled") boolean includeDisabled, Pageable pageable);
}
