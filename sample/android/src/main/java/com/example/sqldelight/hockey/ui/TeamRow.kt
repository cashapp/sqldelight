package com.example.sqldelight.hockey.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.example.sqldelight.hockey.R
import com.example.sqldelight.hockey.data.Team
import com.example.sqldelight.hockey.platform.defaultFormatter

class TeamRow(
  context: Context,
  attrs: AttributeSet,
) : LinearLayout(context, attrs) {
  fun populate(team: Team) {
    findViewById<TextView>(R.id.team_name).text = team.name
    findViewById<TextView>(R.id.coach_name).text = team.coach
    findViewById<TextView>(R.id.founded).text = context.getString(R.string.founded, defaultFormatter.format(team.founded))
  }
}
