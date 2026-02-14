package com.aspiring_creators.aichopaicho.data.repository

import com.aspiring_creators.aichopaicho.data.dao.RepaymentDao
import com.aspiring_creators.aichopaicho.data.entity.Repayment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepaymentRepository @Inject constructor(private val repaymentDao: RepaymentDao) {

    suspend fun insert(repayment: Repayment) {
        repaymentDao.insert(repayment)
    }

    suspend fun update(repayment: Repayment) {
        repaymentDao.update(repayment)
    }

    suspend fun delete(id: String) {
        repaymentDao.delete(id)
    }

    fun getRepaymentsByLoan(loanId: String): Flow<List<Repayment>> {
        return repaymentDao.getRepaymentsByLoan(loanId)
    }

    fun getAllRepayments(): Flow<List<Repayment>> {
        return repaymentDao.getAllRepayments()
    }

    suspend fun getRepaymentById(id: String): Repayment? {
        return repaymentDao.getRepaymentById(id)
    }
}
