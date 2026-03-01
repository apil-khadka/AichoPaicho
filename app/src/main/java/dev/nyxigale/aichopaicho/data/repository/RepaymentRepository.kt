package dev.nyxigale.aichopaicho.data.repository

import dev.nyxigale.aichopaicho.data.dao.RepaymentDao
import dev.nyxigale.aichopaicho.data.entity.Repayment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepaymentRepository @Inject constructor(private val repaymentDao: RepaymentDao) {
    suspend fun insertRepayment(repayment: Repayment) {
        repaymentDao.insertRepayment(repayment)
    }

    suspend fun insertRepayments(repayments: List<Repayment>) {
        repaymentDao.insertRepayments(repayments)
    }

    fun getRepaymentsForRecord(recordId: String): Flow<List<Repayment>> {
        return repaymentDao.getRepaymentsForRecord(recordId)
    }

    suspend fun getRepaymentById(repaymentId: String): Repayment? {
        return repaymentDao.getRepaymentById(repaymentId)
    }

    suspend fun getAllRepayments(): List<Repayment> {
        return repaymentDao.getAllRepayments()
    }
}
