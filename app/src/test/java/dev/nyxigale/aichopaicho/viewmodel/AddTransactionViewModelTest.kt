package dev.nyxigale.aichopaicho.viewmodel

import android.content.Context
import dev.nyxigale.aichopaicho.data.entity.Contact
import dev.nyxigale.aichopaicho.data.repository.ContactRepository
import dev.nyxigale.aichopaicho.data.repository.RecordRepository
import dev.nyxigale.aichopaicho.data.repository.TypeRepository
import dev.nyxigale.aichopaicho.data.repository.UserRepository
import dev.nyxigale.aichopaicho.viewmodel.data.AddTransactionUiState
import dev.nyxigale.aichopaicho.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class AddTransactionViewModelTest {

    private lateinit var viewModel: AddTransactionViewModel
    private lateinit var validateMethod: Method
    private lateinit var resolveContactInputMethod: Method

    @Before
    fun setup() {
        val mockContext = mock(Context::class.java)
        whenever(mockContext.getString(any())).thenAnswer { invocation ->
            val resId = invocation.arguments[0] as Int
            "String_Resource_$resId"
        }

        viewModel = AddTransactionViewModel(
            context = mockContext,
            recordRepository = mock(RecordRepository::class.java),
            typeRepository = mock(TypeRepository::class.java),
            userRepository = mock(UserRepository::class.java),
            contactRepository = mock(ContactRepository::class.java)
        )

        validateMethod = AddTransactionViewModel::class.java.getDeclaredMethod("validate", AddTransactionUiState::class.java)
        validateMethod.isAccessible = true

        resolveContactInputMethod = AddTransactionViewModel::class.java.getDeclaredMethod("resolveContactInput", AddTransactionUiState::class.java)
        resolveContactInputMethod.isAccessible = true
    }

    private fun invokeValidate(state: AddTransactionUiState): Any {
        return validateMethod.invoke(viewModel, state)!!
    }

    private fun getErrorValue(validationErrors: Any, propertyName: String): String? {
        val getterName = "get" + propertyName.replaceFirstChar { it.uppercase() }
        val method = validationErrors.javaClass.getDeclaredMethod(getterName)
        method.isAccessible = true
        return method.invoke(validationErrors) as String?
    }

    @Test
    fun `validate with valid state returns no errors`() {
        val state = AddTransactionUiState(
            contact = Contact(id = "1", name = "Test Contact", userId = "u1", phone = listOf("1234567890"), contactId = "c1"),
            amountInput = "100",
            date = 123456789L
        )

        val errors = invokeValidate(state)

        val hasAnyErrorMethod = errors.javaClass.getDeclaredMethod("hasAnyError")
        hasAnyErrorMethod.isAccessible = true
        val hasError = hasAnyErrorMethod.invoke(errors) as Boolean

        assertFalse(hasError)
        assertNull(getErrorValue(errors, "contactNameError"))
        assertNull(getErrorValue(errors, "contactPhoneError"))
        assertNull(getErrorValue(errors, "amountError"))
        assertNull(getErrorValue(errors, "dateError"))
    }

    @Test
    fun `validate with missing contact name returns error`() {
        val state = AddTransactionUiState(
            contactNameInput = "  ",
            contactPhoneInput = "1234567890",
            amountInput = "100",
            date = 123456789L
        )

        val errors = invokeValidate(state)
        assertEquals("String_Resource_${R.string.error_enter_contact_name}", getErrorValue(errors, "contactNameError"))
    }

    @Test
    fun `validate with missing contact phone returns error`() {
        val state = AddTransactionUiState(
            contactNameInput = "Test User",
            contactPhoneInput = "  ",
            amountInput = "100",
            date = 123456789L
        )

        val errors = invokeValidate(state)
        assertEquals("String_Resource_${R.string.error_enter_contact_number}", getErrorValue(errors, "contactPhoneError"))
    }

    @Test
    fun `validate with invalid contact phone returns error`() {
        val state = AddTransactionUiState(
            contactNameInput = "Test User",
            contactPhoneInput = "123", // Less than 5 digits
            amountInput = "100",
            date = 123456789L
        )

        val errors = invokeValidate(state)
        assertEquals("String_Resource_${R.string.error_enter_valid_contact_number}", getErrorValue(errors, "contactPhoneError"))
    }

    @Test
    fun `validate with missing amount returns error`() {
        val state = AddTransactionUiState(
            contact = Contact(id = "1", name = "Test Contact", userId = "u1", phone = listOf("1234567890"), contactId = "c1"),
            amountInput = "  ",
            date = 123456789L
        )

        val errors = invokeValidate(state)
        assertEquals("String_Resource_${R.string.error_enter_amount}", getErrorValue(errors, "amountError"))
    }

    @Test
    fun `validate with invalid amount returns error`() {
        val state = AddTransactionUiState(
            contact = Contact(id = "1", name = "Test Contact", userId = "u1", phone = listOf("1234567890"), contactId = "c1"),
            amountInput = "-50",
            date = 123456789L
        )

        val errors = invokeValidate(state)
        assertEquals("String_Resource_${R.string.error_enter_valid_amount}", getErrorValue(errors, "amountError"))
    }

    @Test
    fun `validate with null date returns error`() {
        val state = AddTransactionUiState(
            contact = Contact(id = "1", name = "Test Contact", userId = "u1", phone = listOf("1234567890"), contactId = "c1"),
            amountInput = "100",
            date = null
        )

        val errors = invokeValidate(state)
        assertEquals("String_Resource_${R.string.error_select_date}", getErrorValue(errors, "dateError"))
    }

    private fun invokeResolveContactInput(state: AddTransactionUiState): Contact? {
        return resolveContactInputMethod.invoke(viewModel, state) as Contact?
    }

    @Test
    fun `resolveContactInput with valid picked contact returns picked contact`() {
        val pickedContact = Contact(id = "1", name = "Picked Contact", userId = "u1", phone = listOf("1234567890"), contactId = "c1")
        val state = AddTransactionUiState(
            contact = pickedContact,
            contactNameInput = "Some Other Name", // should be ignored
            contactPhoneInput = "0987654321" // should be ignored
        )

        val resolvedContact = invokeResolveContactInput(state)

        assertNotNull(resolvedContact)
        assertEquals(pickedContact.name, resolvedContact?.name)
    }

    @Test
    fun `resolveContactInput with manual input returns new contact`() {
        val state = AddTransactionUiState(
            contact = null,
            contactNameInput = " Manual Contact ",
            contactPhoneInput = " 9876543210 "
        )

        val resolvedContact = invokeResolveContactInput(state)

        assertNotNull(resolvedContact)
        assertEquals("Manual Contact", resolvedContact?.name)
        assertEquals(listOf("9876543210"), resolvedContact?.phone)
        assertEquals("", resolvedContact?.id)
        assertNull(resolvedContact?.userId)
        assertNull(resolvedContact?.contactId)
    }

    @Test
    fun `resolveContactInput with blank manual name returns null`() {
        val state = AddTransactionUiState(
            contact = null,
            contactNameInput = "   ",
            contactPhoneInput = "9876543210"
        )

        val resolvedContact = invokeResolveContactInput(state)
        assertNull(resolvedContact)
    }

    @Test
    fun `resolveContactInput with blank manual phone returns null`() {
        val state = AddTransactionUiState(
            contact = null,
            contactNameInput = "Manual Name",
            contactPhoneInput = "   "
        )

        val resolvedContact = invokeResolveContactInput(state)
        assertNull(resolvedContact)
    }
}
