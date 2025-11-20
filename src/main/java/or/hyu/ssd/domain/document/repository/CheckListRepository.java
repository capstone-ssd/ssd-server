package or.hyu.ssd.domain.document.repository;

import or.hyu.ssd.domain.document.entity.CheckList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckListRepository extends JpaRepository<CheckList, Long> {
}
