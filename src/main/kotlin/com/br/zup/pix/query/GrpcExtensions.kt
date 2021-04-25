package com.br.zup.pix.query

import com.br.zup.ProtoQueryKeyRequest
import com.br.zup.ProtoQueryKeyRequest.ProtoQueryFilterCase.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun ProtoQueryKeyRequest.toModel(validator: Validator): QueryKeyFilter {

    val queryKeyFilter: QueryKeyFilter = when(protoQueryFilterCase) {
        PIXID -> pixId.let {
            QueryKeyFilter.SearchByPixId(
                clientId = it.clientId,
                pixId = it.pixId
            )
        }
        KEY -> QueryKeyFilter.SearchByKey(key = key)
        PROTOQUERYFILTER_NOT_SET -> QueryKeyFilter.Invalid()
    }


    val constraints = validator.validate(queryKeyFilter)
    if(constraints.isNotEmpty()) {
        throw ConstraintViolationException(constraints)
    }

    return queryKeyFilter
}