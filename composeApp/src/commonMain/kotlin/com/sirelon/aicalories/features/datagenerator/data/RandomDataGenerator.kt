package com.sirelon.aicalories.features.datagenerator.data

import com.sirelon.aicalories.features.agile.Estimation
import com.sirelon.aicalories.features.agile.model.Ticket
import com.sirelon.aicalories.features.agile.model.UserStory
import com.sirelon.aicalories.features.agile.team.Team
import com.sirelon.aicalories.features.datagenerator.model.GenerationConfig
import kotlin.random.Random

class RandomDataGenerator {

    private val teamNames = listOf(
        "Team Alpha", "Team Beta", "Team Gamma", "Team Delta", "Team Omega",
        "Thunder Squad", "Phoenix Team", "Dragon Force", "Lightning Crew", "Storm Troopers",
        "Code Warriors", "Pixel Pioneers", "Binary Bandits", "Quantum Leap", "Agile Eagles",
        "Sprint Masters", "Flow State", "Velocity Squad", "Innovation Hub", "Tech Titans",
        "Digital Ninjas", "Cyber Knights", "Data Wizards", "Cloud Crusaders", "Bug Hunters",
        "Aurora League", "Nova Guild", "Helix Syndicate", "Orbit Architects", "Nebula Nomads",
        "Solar Sprints", "Crimson Forge", "Atlas Division", "Starlight Crew", "Midnight Circuit",
        "Iron Orchid", "Glacier Ops", "Radiant Rebels", "Obsidian Axis", "Cobalt Collective"
    )

    private val storyNames = listOf(
        "Epic Quest", "Feature Forge", "User Journey", "Customer Delight", "System Upgrade",
        "Dragon Slayer", "Wizard Integration", "Knight's Mission", "Castle Builder", "Kingdom Enhancement",
        "Cloud Migration", "Data Pipeline", "Authentication Flow", "Payment Gateway", "Notification System",
        "Dashboard Redesign", "Mobile Experience", "Performance Boost", "Security Hardening", "Analytics Integration",
        "Search Optimization", "User Onboarding", "Profile Management", "Reporting Module", "Admin Panel",
        "API Refactoring", "Database Optimization", "Cache Strategy", "Load Balancing", "Microservices",
        "Celestial Sync", "Orbit Planner", "Aurora Pipeline", "Signal Relay", "Trust Ledger",
        "Guardian Mode", "Telemetry Upgrade", "Resilience Sprint", "Edge Routing", "Compass Overhaul",
        "Latency Fixes", "Automation Suite", "Feature Flag Rollout", "Chaos Testing", "Recovery Playbook",
        "On-Call Toolkit", "Customer Pulse", "Intake Portal", "Recommendation Engine", "Metrics Studio"
    )

    private val ticketPrefixes = listOf(
        "Implement", "Design", "Refactor", "Fix", "Update",
        "Create", "Build", "Test", "Optimize", "Configure",
        "Integrate", "Deploy", "Document", "Review", "Validate",
        "Automate", "Harden", "Streamline", "Instrument", "Polish",
        "Migrate", "Expand", "Tune", "Safeguard", "Refine"
    )

    private val ticketSubjects = listOf(
        "UI Component", "API Endpoint", "Database Schema", "Authentication", "Validation",
        "Error Handling", "Unit Tests", "Integration Tests", "Documentation", "CI/CD Pipeline",
        "Code Review", "Security Audit", "Performance Metrics", "Logging System", "Cache Layer",
        "Form Handler", "Data Model", "Service Layer", "Middleware", "Router Config",
        "State Management", "Event Handler", "File Upload", "Email Template", "Web Socket",
        "Feature Flag", "Cache Invalidation", "Rate Limiter", "Observability", "Audit Trail",
        "Migration Script", "Indexer", "Search Relevance", "Usage Quota", "Batch Job",
        "Release Notes", "Fallback Flow", "Retry Policy", "Health Check", "Throttling Rules"
    )

    fun generateTeams(config: GenerationConfig, startingTeamId: Int): List<Team> {
        val teams = mutableListOf<Team>()
        val random = Random(getTimeMillis())
        val usedNames = mutableSetOf<String>()
        val peopleCountRange = config.teamPeopleCount
        val capacityRange = config.teamCapacity
        val riskFactorRange = config.teamRiskFactor

        repeat(config.teamsCount) { index ->
            val teamId = startingTeamId + index
            val teamName = getUniqueName(teamNames, usedNames, random)
            val riskFactor = if (riskFactorRange.min == riskFactorRange.max) {
                riskFactorRange.min
            } else {
                random.nextDouble(riskFactorRange.min, riskFactorRange.max)
            }

            teams.add(
                Team(
                    id = teamId,
                    name = teamName,
                    peopleCount = random.nextInt(
                        peopleCountRange.min,
                        peopleCountRange.max + 1
                    ),
                    capacity = random.nextInt(
                        capacityRange.min,
                        capacityRange.max + 1
                    ),
                    riskFactor = riskFactor
                )
            )
        }

        return teams
    }

    fun generateStoriesForTeam(
        config: GenerationConfig,
        startingStoryId: Int,
        startingTicketId: Int
    ): Pair<List<UserStory>, Int> {
        val stories = mutableListOf<UserStory>()
        val random = Random(getTimeMillis() + startingStoryId)
        var currentStoryId = startingStoryId
        var currentTicketId = startingTicketId
        val usedStoryNames = mutableSetOf<String>()

        repeat(config.storiesPerTeamCount) {
            val ticketCount = random.nextInt(
                config.ticketsPerStory.min,
                config.ticketsPerStory.max + 1
            )
            val tickets = mutableListOf<Ticket>()
            val usedTicketNames = mutableSetOf<String>()

            repeat(ticketCount) {
                val ticketName = "${ticketPrefixes.random(random)} ${ticketSubjects.random(random)}"
                val uniqueTicketName = if (usedTicketNames.contains(ticketName)) {
                    "$ticketName #$currentTicketId"
                } else {
                    usedTicketNames.add(ticketName)
                    ticketName
                }

                tickets.add(
                    Ticket(
                        id = currentTicketId++,
                        name = uniqueTicketName,
                        estimation = Estimation.entries.random(random)
                    )
                )
            }

            val storyName = getUniqueName(storyNames, usedStoryNames, random)

            stories.add(
                UserStory(
                    id = currentStoryId++,
                    name = storyName,
                    tickets = tickets
                )
            )
        }

        return stories to currentTicketId
    }

    private fun getUniqueName(
        namePool: List<String>,
        usedNames: MutableSet<String>,
        random: Random
    ): String {
        val availableNames = namePool.filter { it !in usedNames }
        return if (availableNames.isNotEmpty()) {
            availableNames.random(random).also { usedNames.add(it) }
        } else {
            val baseName = namePool.random(random)
            var uniqueName = baseName
            var counter = 1
            while (uniqueName in usedNames) {
                uniqueName = "$baseName ${counter++}"
            }
            usedNames.add(uniqueName)
            uniqueName
        }
    }
}
