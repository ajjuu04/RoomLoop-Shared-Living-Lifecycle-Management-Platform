package com.project.roomloop.repository;

import com.project.roomloop.entity.Bill;
import com.project.roomloop.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByRoomOrderByBillDateDesc(Room room);
}