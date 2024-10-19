
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.project17.tourbooking.models.*
import kotlinx.coroutines.tasks.await
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date


object FirestoreHelper {

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()


    suspend fun uploadImageToFirebase(uri: Uri): String? {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        return try {
            val uploadTask = imageRef.putFile(uri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            null
        }
    }

    fun getBillById(billId: String, callback: (Bill?) -> Unit) {
        db.collection("bills").document(billId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val bill = document.toObject(Bill::class.java)
                    callback(bill)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun getBillDetailsByBillId1(billId: String, callback: (List<BillDetail>) -> Unit) {
        db.collection("billDetails")
            .whereEqualTo("billId", billId)
            .get()
            .addOnSuccessListener { result ->
                val billDetails = result.mapNotNull { document ->
                    document.toObject(BillDetail::class.java)
                }
                callback(billDetails)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun getTicketDocumentId(tourId: String, callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Truy vấn Firestore để tìm tài liệu có `tourId` mong muốn
        db.collection("tickets")
            .whereEqualTo("tourId", tourId)
            .limit(1) // Lấy tài liệu đầu tiên nếu có nhiều kết quả
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Lấy id của tài liệu đầu tiên
                    val documentId = querySnapshot.documents[0].id
                    callback(documentId)
                } else {
                    // Không tìm thấy tài liệu phù hợp
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi
                Log.e("FirestoreHelper", "Error getting ticket document ID", exception)
                callback(null)
            }
    }

    fun getBillDetailByBillId(billId: String, onComplete: (BillDetail?) -> Unit) {
        db.collection("billDetails")
            .whereEqualTo("billId", billId)
            .limit(1)  // Assuming one BillDetail per Bill, adjust if needed
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val billDetail = documents.documents[0].toObject(BillDetail::class.java)
                    onComplete(billDetail)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreHelper", "Error getting bill detail: $exception")
                onComplete(null)
            }
    }


    fun getTourIdByTicketId(ticketId: String, onSuccess: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val ticketsCollection = db.collection("tickets")

        ticketsCollection
            .whereEqualTo("ticketId", ticketId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val tourId = querySnapshot.documents.firstOrNull()?.getString("tourId")
                onSuccess(tourId)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreHelper", "Error getting tourId by ticketId", exception)
                onSuccess(null)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun Date.toLocalDate(): LocalDate {
        return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }


    suspend fun getOrdersByUser(): Map<String, Int> {
        val bills = FirebaseFirestore.getInstance().collection("bills").get().await()
        val userOrderCount = mutableMapOf<String, Int>()

        for (document in bills.documents) {
            val email = document.getString("email") ?: continue
            userOrderCount[email] = userOrderCount.getOrDefault(email, 0) + 1
        }

        return userOrderCount
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getOrdersByDate(): Map<String, Int> {
        val bills = FirebaseFirestore.getInstance().collection("bills").get().await()
        val dateOrderCount = mutableMapOf<String, Int>()

        for (document in bills.documents) {
            val createdDate = document.getTimestamp("createdDate")?.toDate()?.toLocalDate() ?: continue
            val date = createdDate.toString()
            dateOrderCount[date] = dateOrderCount.getOrDefault(date, 0) + 1
        }

        return dateOrderCount
    }

    suspend fun getOrdersByCategory(): Map<String, Int> {
        val billDetails = FirebaseFirestore.getInstance().collection("billDetails").get().await()
        val tickets = FirebaseFirestore.getInstance().collection("tickets").get().await()

        val categoryOrderCount = mutableMapOf<String, Int>()
        val ticketCategoryMap = tickets.documents.associateBy { it.id }.mapValues { it.value.getString("categoryId") ?: "" }

        for (document in billDetails.documents) {
            val ticketId = document.getString("ticketId") ?: continue
            val categoryId = ticketCategoryMap[ticketId] ?: continue
            categoryOrderCount[categoryId] = categoryOrderCount.getOrDefault(categoryId, 0) + 1
        }

        return categoryOrderCount
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getOrdersByMonth(): Map<String, Int> {
        val bills = FirebaseFirestore.getInstance().collection("bills").get().await()
        val monthOrderCount = mutableMapOf<String, Int>()

        for (document in bills.documents) {
            val createdDate = document.getTimestamp("createdDate")?.toDate()?.toLocalDate() ?: continue
            val month = "${createdDate.year}-${createdDate.monthValue}"
            monthOrderCount[month] = monthOrderCount.getOrDefault(month, 0) + 1
        }

        return monthOrderCount
    }

    suspend fun getTopSellingCategories(limit: Int): List<String> {
        val ordersByCategory = getOrdersByCategory()
        val sortedCategories = ordersByCategory.entries.sortedByDescending { it.value }
        return sortedCategories.take(limit).map { it.key }
    }



    fun deleteBillsAndDetails(billIds: List<String>, callback: (Boolean, Exception?) -> Unit) {
        val batch = FirebaseFirestore.getInstance().batch()

        billIds.forEach { billId ->
            val billRef = FirebaseFirestore.getInstance().collection("bills").document(billId)
            batch.delete(billRef)

            FirebaseFirestore.getInstance().collection("billDetails")
                .whereEqualTo("billId", billId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    querySnapshot.forEach { document ->
                        val billDetailRef = document.reference
                        batch.delete(billDetailRef)
                    }
                    // Commit the batch after adding all delete operations
                    batch.commit().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback(true, null)
                        } else {
                            callback(false, task.exception)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    callback(false, exception)
                }
        }
    }

    suspend fun loadTicketForTour(tourId: String): List<Ticket> {
        return try {
            val querySnapshot = FirebaseFirestore.getInstance()
                .collection("tickets")
                .whereEqualTo("tourId", tourId)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Ticket::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    fun updateTourRating(tourId: String, newAverageRating: Double) {
        val tourRef = db.collection("tours").document(tourId)
        tourRef.update("averageRating", newAverageRating)
            .addOnSuccessListener {
                Log.d("FirestoreHelper", "Tour rating successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreHelper", "Error updating tour rating", e)
            }
    }

    suspend fun loadReviewsForTour(tourId: String): List<Review> {
        return try {
            val querySnapshot = FirebaseFirestore.getInstance()
                .collection("reviews") // Thay đổi thành tên collection reviews nếu khác
                .whereEqualTo("tourId", tourId)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Review::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Trả về danh sách rỗng nếu có lỗi
        }
    }

    suspend fun loadCategories2(): List<CategoryWithId> {
        val categoriesCollection = db.collection("categories")
        val querySnapshot = categoriesCollection.get().await()

        return querySnapshot.documents.map { document ->
            val category = document.toObject(Category::class.java)!!
            CategoryWithId(category, document.id)
        }
    }


    suspend fun loadToursWithIds(): List<TourWithId> {
        val tourCollection = db.collection("tours")
        val snapshot = tourCollection.get().await()
        return snapshot.documents.map { doc ->
            val tour = doc.toObject(Tour::class.java)!!
            TourWithId(doc.id, tour)
        }
    }


    suspend fun loadTours(): List<Tour> {
        val toursCollection = db.collection("tours")
        return toursCollection.get().await().toObjects(Tour::class.java)
    }

    suspend fun loadTickets(): List<Ticket> {
        val ticketsCollection = db.collection("tickets")
        return ticketsCollection.get().await().toObjects(Ticket::class.java)
    }

    suspend fun loadReviews(): List<Review> {
        val reviewsCollection = db.collection("reviews")
        return reviewsCollection.get().await().toObjects(Review::class.java)
    }

    suspend fun loadCategories(): List<Category> {
        val categoriesCollection = db.collection("categories")
        return categoriesCollection.get().await().toObjects(Category::class.java)
    }

    private val categoriesCollection = db.collection("categories")

    // --------------------- Category ---------------------
    // Add a new category
    suspend fun addCategory(category: Category): String {
        val docRef = categoriesCollection.add(category).await()
        return docRef.id
    }

    // Update an existing category
    suspend fun updateCategory(id: String, updatedCategory: Category) {
        categoriesCollection.document(id).set(updatedCategory).await()
    }

    // Delete a category
    suspend fun deleteCategory(id: String) {
        categoriesCollection.document(id).delete().await()
    }

    // Get all categories
    suspend fun getCategories(): List<Pair<String, Category>> {
        return categoriesCollection.get().await().map { document ->
            document.id to document.toObject(Category::class.java)
            document.id to document.toObject(Category::class.java)
        }
    }

    fun getCategories2(onResult: (List<Category>) -> Unit) {
        db.collection("categories")
            .get()
            .addOnSuccessListener { result ->
                val categories = result.documents.mapNotNull { doc ->
                    doc.toObject(Category::class.java)
                }
                onResult(categories)
            }
            .addOnFailureListener { e ->
                onResult(emptyList()) // In case of failure, return an empty list
            }
    }



    // --------------------- Tour ---------------------
    private val toursCollection = db.collection("tours")


    suspend fun getTours(): List<Pair<String, Tour>> {
        return toursCollection.get().await().documents.map { doc ->
            doc.id to doc.toObject(Tour::class.java)!!
        }
    }

    suspend fun getTourById(id: String): Tour? {
        return try {
            val document = toursCollection.document(id).get().await()
            document.toObject(Tour::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getTourById2(tourId: String, onComplete: (Tour?) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("tours")
            .document(tourId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val tour = documentSnapshot.toObject(Tour::class.java)
                onComplete(tour)
            }
            .addOnFailureListener {
                onComplete(null)  // Handle failure
            }
    }



    suspend fun addTour(tour: Tour): String {
        val docRef = toursCollection.add(tour).await()
        return docRef.id
    }

    suspend fun updateTour(id: String, tour: Tour) {
        toursCollection.document(id).set(tour).await()
    }

    suspend fun deleteTour(id: String) {
        toursCollection.document(id).delete().await()


    }

    // --------------------- Ticket ---------------------
    suspend fun getTickets(): Result<Map<String, Ticket>> {
        return try {
            val snapshot = db.collection("tickets").get().await()
            val tickets = snapshot.documents.associate { it.id to it.toObject(Ticket::class.java)!! }
            Result.success(tickets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTicket(ticket: Ticket): Result<Unit> {
        return try {
            val newDocRef = db.collection("tickets").document()
            ticket.copy().let {
                newDocRef.set(it).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTicket(docId: String, ticket: Ticket): Result<Unit> {
        return try {
            db.collection("tickets").document(docId).set(ticket).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTicket(docId: String): Result<Unit> {
        return try {
            db.collection("tickets").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------- Bill ---------------------
    fun getBillByTourId(tourId: String, callback: (Bill?) -> Unit) {
        // Fetch the bill associated with the provided tourId from Firestore
        val billsRef = FirebaseFirestore.getInstance().collection("bills")
        billsRef.whereEqualTo("tourId", tourId).get().addOnSuccessListener { querySnapshot ->
            val bill = querySnapshot.documents.firstOrNull()?.toObject(Bill::class.java)
            callback(bill)
        }.addOnFailureListener { exception ->
            Log.e("FirestoreHelper", "Error fetching bill: ${exception.localizedMessage}")
            callback(null)
        }
    }

    suspend fun getBills(): Result<Map<String, Bill>> {
        return try {
            val snapshot = db.collection("bills").get().await()
            val bills = snapshot.documents.associate { it.id to it.toObject(Bill::class.java)!! }
            Result.success(bills)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addBill(bill: Bill): String? {
        return try {
            val billRef = FirebaseFirestore.getInstance().collection("bills").add(bill).await()
            val billId = billRef.id
            // Cập nhật bill với ID của chính nó
            FirebaseFirestore.getInstance().collection("bills").document(billId).update("id", billId).await()
            billId
        } catch (e: Exception) {
            Log.e("FirestoreHelper", "Error adding Bill: ${e.message}")
            null
        }
    }




    fun deleteBillDetailsByBillId(billId: String, callback: (Boolean, Exception?) -> Unit) {
        val billDetailsRef = FirebaseFirestore.getInstance().collection("billDetails")
        billDetailsRef.whereEqualTo("billId", billId).get().addOnSuccessListener { querySnapshot ->
            val deleteTasks = querySnapshot.documents.map { doc ->
                doc.reference.delete()
            }
            Tasks.whenAllComplete(deleteTasks).addOnSuccessListener {
                callback(true, null)
            }.addOnFailureListener { exception ->
                Log.e("FirestoreHelper", "Error deleting bill details: ${exception.localizedMessage}")
                callback(false, exception)
            }
        }.addOnFailureListener { exception ->
            Log.e("FirestoreHelper", "Error fetching bill details: ${exception.localizedMessage}")
            callback(false, exception)
        }
    }




    suspend fun updateBill(docId: String, bill: Bill): Result<Unit> {
        return try {
            db.collection("bills").document(docId).set(bill).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBill(docId: String): Result<Unit> {
        return try {
            db.collection("bills").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------- BillDetail ---------------------
    suspend fun getBillsByEmail(email: String, callback: (List<Bill>) -> Unit) {
        db.collection("bills")
            .whereEqualTo("email", email)  // Assuming 'email' is a field in 'bills' collection
            .get()
            .addOnSuccessListener { querySnapshot ->
                val bills = querySnapshot.documents.mapNotNull { document ->
                    val bill = document.toObject(Bill::class.java)
                    bill?.let {
                        it.copy(id = document.id)  // Add Firestore-generated document ID to the Bill
                    }
                }
                callback(bills)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreHelper", "Error fetching bills: ${exception.message}")
                callback(emptyList())
            }
    }

    fun getBillDetailsByBillId(billId: String, onComplete: (List<BillDetail>) -> Unit) {
        db.collection("billDetails")
            .whereEqualTo("billId", billId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val billDetails = querySnapshot.documents.mapNotNull { it.toObject(BillDetail::class.java) }
                onComplete(billDetails)
            }
    }

    fun getTicketById(ticketId: String, onComplete: (Ticket?) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("tickets")
            .document(ticketId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val ticket = documentSnapshot.toObject(Ticket::class.java)
                onComplete(ticket)
            }
    }

    fun getTourById(tourId: String, onComplete: (Tour?) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("tours")
            .document(tourId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val tour = documentSnapshot.toObject(Tour::class.java)
                onComplete(tour)
            }
    }

    suspend fun getBillDetails(): Result<Map<String, BillDetail>> {
        return try {
            val snapshot = db.collection("bill_details").get().await()
            val billDetails = snapshot.documents.associate { it.id to it.toObject(BillDetail::class.java)!! }
            Result.success(billDetails)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addBillDetail(billDetail: BillDetail): Boolean {
        return try {
            db.collection("billDetails").add(billDetail).await()
            true // Success
        } catch (e: Exception) {
            false // Failure
        }
    }
    suspend fun updateBillDetail(docId: String, billDetail: BillDetail): Result<Unit> {
        return try {
            db.collection("bill_details").document(docId).set(billDetail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBillDetail(docId: String): Result<Unit> {
        return try {
            db.collection("bill_details").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------- Account ---------------------
    private val accountsCollection = db.collection("accounts")

    // Add a new account with hashed password
    suspend fun addAccount(account: Account) {
        val hashedPassword = BCrypt.hashpw(account.password, BCrypt.gensalt())
        val accountWithHashedPassword = account.copy(password = hashedPassword)
        accountsCollection.add(accountWithHashedPassword).await()
    }

    suspend fun updateAccount(username: String, updatedAccount: Account) {
        try {
            Log.d("FirestoreHelper", "Attempting to update account with username: $username")

            // Query Firestore to find the document with the matching username
            val querySnapshot = accountsCollection.whereEqualTo("username", username).get().await()

            if (querySnapshot.isEmpty) {
                Log.d("FirestoreHelper", "No account found with username: $username")
                return
            }

            // Assume we only need to update the first document that matches the username
            val document = querySnapshot.documents.first()
            val documentId = document.id
            val currentAccount = document.toObject(Account::class.java)

            // Prepare the updated account fields
            val accountToUpdate = Account(
                username = updatedAccount.username.takeIf { it.isNotEmpty() } ?: currentAccount?.username ?: "",
                email = updatedAccount.email.takeIf { it.isNotEmpty() } ?: currentAccount?.email ?: "",
                password = updatedAccount.password.takeIf { it.isNotEmpty() }?.let { BCrypt.hashpw(it, BCrypt.gensalt()) } ?: currentAccount?.password ?: "",
                avatar = updatedAccount.avatar.takeIf { it.isNotEmpty() } ?: currentAccount?.avatar ?: "",
                role = updatedAccount.role.takeIf { it.isNotEmpty() } ?: currentAccount?.role ?: ""
            )

            // Update the document in Firestore
            accountsCollection.document(documentId).set(accountToUpdate).await()
            Log.d("FirestoreHelper", "Successfully updated account with ID: $documentId")
        } catch (e: Exception) {
            Log.e("FirestoreHelper", "Error updating account with username: $username", e)
        }
    }


    suspend fun deleteAccount(email: String) {
        try {
            Log.d("FirestoreHelper", "Attempting to delete account with email: $email")

            // Query Firestore to find the document with the matching email
            val querySnapshot = accountsCollection.whereEqualTo("email", email).get().await()

            if (querySnapshot.isEmpty) {
                Log.d("FirestoreHelper", "No account found with email: $email")
                return
            }

            // Delete all documents matching the email
            for (document in querySnapshot.documents) {
                val documentId = document.id
                accountsCollection.document(documentId).delete().await()
                Log.d("FirestoreHelper", "Successfully deleted account with ID: $documentId")
            }
        } catch (e: Exception) {
            Log.e("FirestoreHelper", "Error deleting account with email: $email", e)
        }
    }

    // Load all accounts
    suspend fun loadAccounts(): List<Account> {
        val snapshot = accountsCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Account::class.java) }
    }

    // Check password against hashed password
    fun checkPassword(hashedPassword: String, plainPassword: String): Boolean {
        return BCrypt.checkpw(plainPassword, hashedPassword)
    }

    suspend fun updatePassword(email: String, newPassword: String): Boolean {
        val snapshot = accountsCollection.whereEqualTo("email", email).get().await()
        if (snapshot.isEmpty) return false

        val documentRef = snapshot.documents.first().reference
        val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        documentRef.update("password", hashedPassword).await()
        return true
    }

    suspend fun isEmailExists(email: String): Boolean {
        val snapshot = accountsCollection.whereEqualTo("email", email).get().await()
        return !snapshot.isEmpty
    }

    fun getCustomerByEmail(email: String, callback: (Customer?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("customers")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    callback(null)
                } else {
                    val customer = result.documents.firstOrNull()?.toObject(Customer::class.java)
                    callback(customer)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreHelper", "Error getting documents: ", exception)
                callback(null)
            }
    }

    fun getAccountByEmail(email: String, callback: (Account?) -> Unit) {
        // Reference to the 'accounts' collection
        val accountsCollection = db.collection("accounts")

        // Query to find the account by email
        val query = accountsCollection.whereEqualTo("email", email)

        // Execute the query asynchronously
        query.get()
            .addOnSuccessListener { querySnapshot ->
                // Check if we have any results
                if (querySnapshot.isEmpty) {
                    callback(null)
                } else {
                    // Get the first result (should be unique by email)
                    val documentSnapshot = querySnapshot.documents.firstOrNull()
                    val account = documentSnapshot?.toObject(Account::class.java)
                    callback(account)
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Log.e("FirestoreHelper", "Error fetching account by email", exception)
                callback(null)
            }
    }

    fun getAvatarUrlFromAccount(email: String, callback: (String?) -> Unit) {
        getAccountByEmail(email) { account ->
            callback(account?.avatar)
        }
    }


    // --------------------- Customer ---------------------
    suspend fun getCustomers(): Result<Map<String, Customer>> {
        return try {
            val snapshot = db.collection("customers").get().await()
            val customers = snapshot.documents.associate { it.id to it.toObject(Customer::class.java)!! }
            Result.success(customers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCustomer(customer: Customer): Result<Unit> {
        return try {
            val newDocRef = db.collection("customers").document()
            customer.copy().let {
                newDocRef.set(it).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCustomer(docId: String, customer: Customer): Result<Unit> {
        return try {
            db.collection("customers").document(docId).set(customer).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCustomer(docId: String): Result<Unit> {
        return try {
            db.collection("customers").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------- Review ---------------------
    suspend fun getReviews(): Result<Map<String, Review>> {
        return try {
            val snapshot = db.collection("reviews").get().await()
            val reviews = snapshot.documents.associate { it.id to it.toObject(Review::class.java)!! }
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getReviewsCollection(): CollectionReference {
        return db.collection("reviews")
    }

    fun addReview(review: Review, onSuccess: (Boolean, Exception?) -> Unit) {
        val reviewToSave = review.copy(rating = (review.rating * 10).toInt() / 10.0) // Round to 1 decimal place

        getReviewsCollection().add(reviewToSave)
            .addOnSuccessListener {
                onSuccess(true, null)
            }
            .addOnFailureListener { exception ->
                onSuccess(false, exception)
            }
    }

    suspend fun updateReview(docId: String, review: Review): Result<Unit> {
        return try {
            db.collection("reviews").document(docId).set(review).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReview(docId: String): Result<Unit> {
        return try {
            db.collection("reviews").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------- CustomerOrderTicket ---------------------
    suspend fun getCustomerOrderTickets(): Result<Map<String, CustomerOrderTicket>> {
        return try {
            val snapshot = db.collection("customer_order_tickets").get().await()
            val customerOrderTickets = snapshot.documents.associate { it.id to it.toObject(CustomerOrderTicket::class.java)!! }
            Result.success(customerOrderTickets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCustomerOrderTicket(customerOrderTicket: CustomerOrderTicket): Result<Unit> {
        return try {
            val newDocRef = db.collection("customer_order_tickets").document()
            customerOrderTicket.copy().let {
                newDocRef.set(it).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCustomerOrderTicket(docId: String, customerOrderTicket: CustomerOrderTicket): Result<Unit> {
        return try {
            db.collection("customer_order_tickets").document(docId).set(customerOrderTicket).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCustomerOrderTicket(docId: String): Result<Unit> {
        return try {
            db.collection("customer_order_tickets").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}