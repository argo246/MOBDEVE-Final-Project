package com.mobdeve.s19.group2.mco2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mobdeve.s19.group2.mco2.adapter.CourtAdapter
import com.mobdeve.s19.group2.mco2.adapter.GroupAdapter
import com.mobdeve.s19.group2.mco2.utils.DatabaseHelper


class MainActivity : AppCompatActivity() {

    private lateinit var groupAdapter: GroupAdapter
    private lateinit var courtAdapter: CourtAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Initialize SQLite
        val dbHelper = DatabaseHelper(this)

        // Setup courts RecyclerView
        val courtsRecyclerView: RecyclerView = findViewById(R.id.recyclerViewCourt)
        courtsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val courts = dbHelper.getCourts()
        courtAdapter = CourtAdapter(courts.toMutableList())
        courtsRecyclerView.adapter = courtAdapter

        // Setup groups RecyclerView
        val groupsRecyclerView: RecyclerView = findViewById(R.id.recycler_view_groups)
        groupsRecyclerView.layoutManager = LinearLayoutManager(this)
        val groups = dbHelper.getGroups()
        groupAdapter = GroupAdapter(groups.toMutableList())
        groupsRecyclerView.adapter = groupAdapter

        // Filter button setup
        val filterButton: Button = findViewById(R.id.filterButton)
        filterButton.setOnClickListener {
            showFilterDialog()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Perform no action for Home; it's already active
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }


    data class FilterCriteria(
        val dateTime: String?,
        val queueMaster: String?,
        val locationOrName: String?
    )

    private fun filterContent(criteria: FilterCriteria) {
        val dbHelper = DatabaseHelper(this)

        // Filter groups based on the criteria
        val filteredGroups = dbHelper.getGroups().filter { group ->
            val matchesDateTime = criteria.dateTime?.let {
                group.queueDateTime.contains(it.trim(), ignoreCase = true)
            } ?: true

            val matchesQueueMaster = criteria.queueMaster?.let {
                group.queueMaster.contains(it.trim(), ignoreCase = true)
            } ?: true

            val matchesLocationOrName = criteria.locationOrName?.let { query ->
                query.trim().let { trimmedQuery ->
                    group.groupName.contains(trimmedQuery, ignoreCase = true) ||
                            group.address.contains(trimmedQuery, ignoreCase = true) ||
                                group.courtName.contains(trimmedQuery, ignoreCase = true)
                }
            } ?: true

            matchesDateTime && matchesQueueMaster && matchesLocationOrName
        }
        groupAdapter.updateData(filteredGroups.toMutableList())

        // Filter courts based on the criteria
        val filteredCourts = dbHelper.getCourts().filter { court ->
            val matchesLocationOrName = criteria.locationOrName?.let { query ->
                query.trim().let { trimmedQuery ->
                    court.courtName.contains(trimmedQuery, ignoreCase = true) ||
                            court.address.contains(trimmedQuery, ignoreCase = true)
                }
            } ?: true

            matchesLocationOrName
        }
        courtAdapter.updateData(filteredCourts.toMutableList())
    }

    @SuppressLint("MissingInflatedId")
    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.filter_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Filter Groups and Courts")
            .setView(dialogView)
            .setPositiveButton("Apply") { _, _ ->
                val dateTime = dialogView.findViewById<EditText>(R.id.filterDateTime).text.toString()
                val queueMaster = dialogView.findViewById<EditText>(R.id.filterQueueMaster).text.toString()
                val location = dialogView.findViewById<EditText>(R.id.filterLocation).text.toString()

                val criteria = FilterCriteria(
                    dateTime = if (dateTime.isBlank()) null else dateTime,
                    queueMaster = if (queueMaster.isBlank()) null else queueMaster,
                    locationOrName = if (location.isBlank()) null else location
                )
                filterContent(criteria)
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }
}

@Composable
fun MainContent() {

}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {

}