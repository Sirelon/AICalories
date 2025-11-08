package com.sirelon.aicalories.supabase.model

import com.sirelon.aicalories.supabase.response.ReportAnalysisEntryResponse
import com.sirelon.aicalories.supabase.response.ReportAnalysisSummaryResponse

data class AnalyseReportData(
    val summary: ReportAnalysisSummaryResponse,
    val entries: List<ReportAnalysisEntryResponse>,
)