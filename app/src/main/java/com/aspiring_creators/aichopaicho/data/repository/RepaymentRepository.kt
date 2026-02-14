package com.aspiring_creators.aichopaicho.data.repository

import com.aspiring_creators.aichopaicho.data.dao.RepaymentDao
import com.aspiring_creators.aichopaicho.data.entity.Repayment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepaymentRepository @Inject constructor(private val repaymentDao: RepaymentDao) {
    suspend fun insertRepayment(repayment: Repayment) {
        repaymentDao.insertRepayment(repayment)
    }

    fun getRepaymentsForRecord(recordId: String): Flow<List<Repayment>> {
        return repaymentDao.getRepaymentsForRecord(recordId)
    }

    suspend fun getRepaymentById(repaymentId: String): Repayment? {
        return repaymentDao.getRepaymentById(repaymentId)
    }
}
