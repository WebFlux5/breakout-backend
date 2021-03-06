package backend.model.payment

import backend.model.BasicEntity
import org.apache.commons.codec.binary.Hex
import org.javamoney.moneta.Money
import java.math.BigDecimal
import java.security.MessageDigest
import java.util.*
import javax.persistence.CascadeType.MERGE
import javax.persistence.CascadeType.PERSIST
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany

@Entity
abstract class Invoice : BasicEntity {

    @OneToMany(cascade = arrayOf(MERGE, PERSIST), mappedBy = "invoice", orphanRemoval = true)
    private val payments: MutableList<Payment> = mutableListOf()

    @Column
    lateinit var amount: Money
        private set

    @Column(unique = true)
    lateinit var purposeOfTransfer: String
        protected set

    @Column(unique = true)
    lateinit var purposeOfTransferCode: String
        protected set

    protected constructor()

    constructor(amount: Money) {
        this.amount = amount
    }

    fun addPayment(payment: Payment) {
        checkPaymentEligability(payment)
        payment.invoice = this
        this.payments.add(payment)
    }

    fun isFullyPaid(): Boolean {
        return this.amountOfCurrentPayments().isGreaterThanOrEqualTo(this.amount)
    }

    fun amountOfCurrentPayments(): Money {
        val amounts = this.payments.map { it.amount }
        val zero = Money.of(BigDecimal.ZERO, "EUR")
        return amounts.fold(zero) { total, next -> total.add(next) }
    }

    fun getPayments(): Iterable<Payment> {
        val paymentsAsImmutableList: List<Payment> = this.payments
        return paymentsAsImmutableList
    }

    @Throws
    abstract fun checkPaymentEligability(payment: Payment)

    abstract fun generatePurposeOfTransfer(): String

    fun generateRandomPurposeOfTransferCode(): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")

        val randomString = UUID.randomUUID().toString().substring(0, 4).toUpperCase()
        messageDigest.update(randomString.toByteArray())
        val randomStringCheckSum = Hex.encodeHexString(messageDigest.digest()).substring(0, 2).toUpperCase()

        return randomString + randomStringCheckSum
    }
}
