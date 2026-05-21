package com.vitfelix.mech_shop.application.dto.request

data class UpdateWorkOrderItemsRequest(
    val serviceIds: List<Long> = emptyList(),
    val partIds: List<Long> = emptyList()
)
