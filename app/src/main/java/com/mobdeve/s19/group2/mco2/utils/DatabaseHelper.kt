package com.mobdeve.s19.group2.mco2.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mobdeve.s19.group2.mco2.R
import com.mobdeve.s19.group2.mco2.model.CourtName
import com.mobdeve.s19.group2.mco2.model.QueueingGroup

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "test91App.db"
        private const val DATABASE_VERSION = 1

        // Courts table
        private const val TABLE_COURTS = "courts"
        private const val COLUMN_COURT_ID = "id"
        private const val COLUMN_COURT_NAME = "name"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_BUSINESS_HOURS = "business_hours"
        private const val COLUMN_IMAGE_ID = "image_id"
        private const val COLUMN_MAP_RES_ID = "map_res_id"

        // Groups table
        private const val TABLE_GROUPS = "groups"
        private const val COLUMN_GROUP_ID = "id"
        private const val COLUMN_GROUP_NAME = "name"
        private const val COLUMN_GROUP_COURT_NAME = "court_name"
        private const val COLUMN_GROUP_ADDRESS = "address"
        private const val COLUMN_GROUP_QUEUE_MASTER = "queue_master"
        private const val COLUMN_QUEUE_DATE_TIME = "queue_date_time"
        private const val COLUMN_GROUP_IMAGE_ID = "image_id"
        private const val COLUMN_GROUP_MAP_RES_ID = "map_res_id"

        // Queue Registrations table
        private const val TABLE_QUEUE_REGISTRATIONS = "queue_registrations"
        private const val COLUMN_REGISTRATION_ID = "id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_GROUP_ID_REF = "group_id"
        private const val COLUMN_REGISTRATION_DATE = "registration_date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createCourtsTable = """
        CREATE TABLE $TABLE_COURTS (
            $COLUMN_COURT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_COURT_NAME TEXT NOT NULL,
            $COLUMN_ADDRESS TEXT NOT NULL,
            $COLUMN_BUSINESS_HOURS TEXT,
            $COLUMN_IMAGE_ID INTEGER,
            $COLUMN_MAP_RES_ID INTEGER
        )
    """
        db.execSQL(createCourtsTable)

        val createGroupsTable = """
        CREATE TABLE $TABLE_GROUPS (
            $COLUMN_GROUP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_GROUP_NAME TEXT NOT NULL,
            $COLUMN_GROUP_COURT_NAME TEXT NOT NULL,
            $COLUMN_GROUP_ADDRESS TEXT NOT NULL,
            $COLUMN_GROUP_QUEUE_MASTER TEXT NOT NULL,
            $COLUMN_QUEUE_DATE_TIME TEXT,
            $COLUMN_GROUP_IMAGE_ID INTEGER,
            $COLUMN_GROUP_MAP_RES_ID INTEGER
        )
    """
        db.execSQL(createGroupsTable)

        val createQueueRegistrationsTable = """
            CREATE TABLE $TABLE_QUEUE_REGISTRATIONS (
                $COLUMN_REGISTRATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID TEXT NOT NULL,
                $COLUMN_GROUP_ID_REF INTEGER NOT NULL,
                $COLUMN_REGISTRATION_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY($COLUMN_GROUP_ID_REF) REFERENCES $TABLE_GROUPS($COLUMN_GROUP_ID)
            )
        """
        db.execSQL(createQueueRegistrationsTable)

        // Pre-populate data
        populateInitialData(db)
    }

    // Insert or remove a user from a group
    fun updateGoingCount(groupName: String, userId: String, isGoing: Boolean) {
        val db = writableDatabase
        val groupId = getGroupIdByName(groupName)
        if (groupId != -1) {
            val values = ContentValues().apply {
                put(COLUMN_USER_ID, userId)
                put(COLUMN_GROUP_ID_REF, groupId)
            }

            if (isGoing) {
                db.insert(TABLE_QUEUE_REGISTRATIONS, null, values)
            } else {
                db.delete(
                    TABLE_QUEUE_REGISTRATIONS,
                    "$COLUMN_USER_ID = ? AND $COLUMN_GROUP_ID_REF = ?",
                    arrayOf(userId, groupId.toString())
                )
            }
        }
        db.close()
    }

    fun isUserGoing(groupName: String, userId: String): Boolean {
        val db = readableDatabase
        val groupId = getGroupIdByName(groupName)
        if (groupId != -1) {
            val cursor = db.query(
                TABLE_QUEUE_REGISTRATIONS,
                arrayOf(COLUMN_USER_ID),
                "$COLUMN_USER_ID = ? AND $COLUMN_GROUP_ID_REF = ?",
                arrayOf(userId, groupId.toString()),
                null, null, null
            )
            val isGoing = cursor.count > 0
            cursor.close()
            db.close()
            return isGoing
        }
        db.close()
        return false
    }

    fun getGoingCount(groupName: String): Int {
        val db = readableDatabase
        val groupId = getGroupIdByName(groupName)
        if (groupId != -1) {
            val cursor = db.query(
                TABLE_QUEUE_REGISTRATIONS,
                arrayOf(COLUMN_USER_ID),
                "$COLUMN_GROUP_ID_REF = ?",
                arrayOf(groupId.toString()),
                null, null, null
            )
            val count = cursor.count
            cursor.close()
            db.close()
            return count
        }
        db.close()
        return 0
    }

    private fun getGroupIdByName(groupName: String): Int {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_GROUPS,
            arrayOf(COLUMN_GROUP_ID),
            "$COLUMN_GROUP_NAME = ?",
            arrayOf(groupName),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GROUP_ID))
        } else {
            -1  // Not found
        }
    }

    private fun populateInitialData(db: SQLiteDatabase) {
        // Insert courts data
        val courts = listOf(
            CourtName(R.drawable.sureshot, "Sureshot Sportsville", "2848, 19 Abad Santos Ave, Tondo, Manila", "07:00AM - 10:30PM", R.drawable.map_sureshot),
            CourtName(R.drawable.dragonsmash, "DragonSmash Badminton Center", "2227 Chino Roces Ave, Makati, Metro Manila", "06:00AM - 11:59PM", R.drawable.map_dragonsmash),
            CourtName(R.drawable.centro, "Centro Atletico Badminton", "25 West Road Corner North Road Cubao, Quezon City", "12:00NN - 10:00PM",  R.drawable.map_centro),
            CourtName(R.drawable.orlando, "Orlando Suites", "2489 Singalong Cor Estrada, Manila", "09:00AM - 06:00PM", R.drawable.map_orlando),
            CourtName(R.drawable.sureshot, "Olmpic Badminton Center", "H3P8+WX7, AutoCamp Access Rd, Pasig, Metro Manila", "06:00AM - 12:00AM", R.drawable.map_sureshot),
            CourtName(R.drawable.dragonsmash, "Court Zone Badminton Center", "1142 President Quirino Ext, Paco, Manila, 1007 Metro Manila", "09:00AM - 09:00PM", R.drawable.map_dragonsmash),
            CourtName(R.drawable.centro, "Powerplay Badminton Center", "1118 Jorge Bocobo St, Ermita, Manila, 1000 Metro Manila", "10:00AM - 10:00PM",  R.drawable.map_centro),
            CourtName(R.drawable.orlando, "Hemady Badminton Court", "9 Doña Hemady St, Valencia, Quezon City, 1112 Metro Manila", "06:00AM - 11:00PM", R.drawable.map_orlando),
            CourtName(R.drawable.centro, "Wilson Badminton Court", "126 Speaker Perez Street corner Retiro Street, Quezon City, Metro Manila", "10:00AM - 11:00PM",  R.drawable.map_centro),
            CourtName(R.drawable.orlando, "Smart Shot Badminton And Sports Center", "Zone 075, 1620 F Agoncillo St, Malate, Manila, 1004 Metro Manila", "06:30AM - 12:00AM", R.drawable.map_orlando),

        )

        courts.forEach { court ->
            val values = ContentValues().apply {
                put(COLUMN_COURT_NAME, court.courtName)
                put(COLUMN_ADDRESS, court.address)
                put(COLUMN_BUSINESS_HOURS, court.businessHours)
                put(COLUMN_IMAGE_ID, court.imageId)
                put(COLUMN_MAP_RES_ID, court.mapResId)
            }
            db.insert(TABLE_COURTS, null, values)
        }

        // Insert groups data
        val groups = listOf(
            QueueingGroup(R.drawable.friday1, "Friday Badminton Group", "Sureshot Sportsville", "2848, 19 Abad Santos Ave, Tondo, Manila", "Jed", "Friday 08:30PM - 10:30PM", R.drawable.map_sureshot),
            QueueingGroup(R.drawable.chillax, "Chillax Sports Club", "DragonSmash Badminton Center", "2227 Chino Roces Ave, Makati, Metro Manila", "Jake", "Saturday 12:00NN - 04:00PM", R.drawable.map_dragonsmash),
            QueueingGroup(R.drawable.monday, "Monday Badminton Group", "Centro Atletico Badminton", "25 West Road Corner North Road Cubao, Quezon City", "Kerwin", "Monday 7:30PM - 11:00PM", R.drawable.map_centro),
            QueueingGroup(R.drawable.dlsubadsoc, "DLSU Badminton Society","Orlando Suites", "2489 Singalong Cor Estrada, Manila", "Josh", "Wednesday 09:00AM - 06:00PM", R.drawable.map_orlando),
            QueueingGroup(R.drawable.friday2, "Johnny & Friends Badminton Group", "Sureshot Sportsville", "2848, 19 Abad Santos Ave, Tondo, Manila", "Johnny", "Friday 06:00AM - 08:30PM", R.drawable.map_sureshot),
            QueueingGroup(R.drawable.chillax, "Shuttle Mafia", "Powerplay Badminton Center", "1118 Jorge Bocobo St, Ermita, Manila, 1000 Metro Manila", "Kenzie", "Sunday 01:00PM - 05:00PM", R.drawable.map_dragonsmash),
            QueueingGroup(R.drawable.monday, "Dropsmash Badminton Queueing Group", "Wilson Badminton Court", "126 Speaker Perez Street corner Retiro Street, Quezon City, Metro Manila", "Rene", "Sunday 03:00PM - 07:00PM", R.drawable.map_centro),
            QueueingGroup(R.drawable.dlsubadsoc, "Racket Squad","Hemady Badminton Court", "9 Doña Hemady St, Valencia, Quezon City, 1112 Metro Manila", "Melvin", "Sunday 09:00AM - 11:00AM", R.drawable.map_orlando)
        )

        groups.forEach { group ->
            val values = ContentValues().apply {
                put(COLUMN_GROUP_NAME, group.groupName)
                put(COLUMN_GROUP_COURT_NAME, group.courtName)
                put(COLUMN_ADDRESS, group.address)
                put(COLUMN_GROUP_QUEUE_MASTER, group.queueMaster)
                put(COLUMN_QUEUE_DATE_TIME, group.queueDateTime)
                put(COLUMN_IMAGE_ID, group.imageId)
                put(COLUMN_GROUP_MAP_RES_ID, group.mapResId)
            }
            db.insert(TABLE_GROUPS, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COURTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GROUPS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUEUE_REGISTRATIONS")
        onCreate(db)
    }

    fun getCourts(): List<CourtName> {
        val db = readableDatabase
        val cursor = db.query(TABLE_COURTS, null, null, null, null, null, null)
        val courts = mutableListOf<CourtName>()
        while (cursor.moveToNext()) {
            courts.add(
                CourtName(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BUSINESS_HOURS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAP_RES_ID))
                )
            )
        }
        cursor.close()
        db.close()
        return courts
    }

    fun getGroups(): List<QueueingGroup> {
        val db = readableDatabase
        val cursor = db.query(TABLE_GROUPS, null, null, null, null, null, null)
        val groups = mutableListOf<QueueingGroup>()

        while (cursor.moveToNext()) {
            groups.add(
                QueueingGroup(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_COURT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_QUEUE_MASTER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUEUE_DATE_TIME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GROUP_MAP_RES_ID))
                )
            )
        }
        cursor.close()
        db.close()
        return groups
    }
}