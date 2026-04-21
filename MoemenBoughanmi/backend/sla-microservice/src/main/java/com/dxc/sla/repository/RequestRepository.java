package com.dxc.sla.repository;

import com.dxc.sla.entity.Request;
import com.dxc.sla.entity.RequestStatus;
import com.dxc.sla.entity.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByEmployeeId(Long employeeId);

    List<Request> findByStatus(RequestStatus status);

    List<Request> findByTypeAndStatus(RequestType type, RequestStatus status);

    List<Request> findByStatusOrderByRequestDateDesc(RequestStatus status);

    // Demandes HR (Attestations)
    List<Request> findByTypeInAndStatus(List<RequestType> types, RequestStatus status);

    // Demandes Manager (Conges, Assurances)
    List<Request> findByTypeInAndStatusOrderByRequestDateDesc(List<RequestType> types, RequestStatus status);

    // Toutes demandes par type (toutes statuts) - pour statistiques
    List<Request> findByTypeInOrderByRequestDateDesc(List<RequestType> types);

    // Toutes demandes (admin) - pour statistiques
    List<Request> findAllByOrderByRequestDateDesc();

    boolean existsByRequestNumber(String requestNumber);
}
