package com.landside.shadowstate

import com.landside.shadowstate_annotation.InjectAgent

object AgentInjection {

    fun inject(instance: Any, agent: StateAgent<*, *>) {
        val fields = instance.javaClass.declaredFields
        fields.forEach {
            it.isAccessible = true
            if (it.isAnnotationPresent(InjectAgent::class.java)) {
                try {
                    it.set(instance, agent)
                } catch (e: IllegalAccessException) {
                } catch (e: IllegalArgumentException) {

                }
            }
        }
    }

    fun getAgents(instance: Any):List<Class<*>>{
        val result = mutableListOf<Class<*>>()
        val fields = instance.javaClass.declaredFields
        fields.forEach {
            it.isAccessible = true
            if (it.isAnnotationPresent(InjectAgent::class.java)) {
                if (StateAgent::class.java.isAssignableFrom((it.genericType as Class<*>))) {
                    result.add(it.genericType as Class<*>)
                }
            }
        }
        return result
    }

}