package com.maktabati.app.ui.viewmodel

import com.maktabati.app.data.importer.ConvertibleBookProposal
import com.maktabati.app.data.importer.MultiBookArchiveProposal
import kotlinx.coroutines.flow.MutableStateFlow

class BookImportQueueManager {
    val pendingMultiBookProposal = MutableStateFlow<MultiBookArchiveProposal?>(null)
    private val proposalQueue = ArrayDeque<MultiBookArchiveProposal>()

    val pendingConversionProposal = MutableStateFlow<ConvertibleBookProposal?>(null)
    private val conversionProposalQueue = ArrayDeque<ConvertibleBookProposal>()

    fun enqueueProposals(proposals: List<MultiBookArchiveProposal>) {
        if (proposals.isEmpty()) return
        for (p in proposals) {
            proposalQueue.addLast(p)
        }
        if (pendingMultiBookProposal.value == null) {
            pendingMultiBookProposal.value = proposalQueue.removeFirstOrNull()
        }
    }

    fun advanceNextProposalOrClear() {
        pendingMultiBookProposal.value = proposalQueue.removeFirstOrNull()
    }

    fun enqueueConversionProposals(proposals: List<ConvertibleBookProposal>) {
        if (proposals.isEmpty()) return
        for (p in proposals) {
            conversionProposalQueue.addLast(p)
        }
        if (pendingConversionProposal.value == null) {
            pendingConversionProposal.value = conversionProposalQueue.removeFirstOrNull()
        }
    }

    fun advanceNextConversionProposalOrClear() {
        pendingConversionProposal.value = conversionProposalQueue.removeFirstOrNull()
    }
}
