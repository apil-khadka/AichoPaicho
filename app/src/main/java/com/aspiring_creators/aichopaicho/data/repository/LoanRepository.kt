package com.aspiring_creators.aichopaicho.data.repository

import com.aspiring_creators.aichopaicho.data.dao.LoanDao
import com.aspiring_creators.aichopaicho.data.entity.Loan
import com.aspiring_creators.aichopaicho.data.entity.LoanWithContact
import com.aspiring_creators.aichopaicho.data.entity.LoanWithRepayments
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(private val loanDao: LoanDao) {

    suspend fun insert(loan: Loan) {
        loanDao.insert(loan)
    }

    suspend fun update(loan: Loan) {
        loanDao.update(loan)
    }

    suspend fun delete(id: String) {
        loanDao.delete(id)
    }

    fun getLoansByContact(contactId: String): Flow<List<Loan>> {
        return loanDao.getLoansByContact(contactId)
    }

    fun getLoansWithRepayments(contactId: String): Flow<List<LoanWithRepayments>> {
        return loanDao.getLoansWithRepayments(contactId)
    }

    fun getAllLoans(): Flow<List<Loan>> {
        return loanDao.getAllLoans()
    }

    suspend fun getLoanById(id: String): Loan? {
        return loanDao.getLoanById(id)
    }

    fun getRecentLoans(limit: Int): Flow<List<LoanWithContact>> {
        return loanDao.getRecentLoansWithContact(limit)
    }
}
