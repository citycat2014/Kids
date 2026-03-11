package com.example.kids.ui.kid

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.repository.KidRepository
import com.example.kids.ui.screens.KidListItemUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class KidListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = KidsDatabase.getInstance(application)
    private val repository = KidRepository(database.kidDao())

    private val _kids = MutableStateFlow<List<KidListItemUi>>(emptyList())
    val kids: StateFlow<List<KidListItemUi>> = _kids.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeKids()
                .map { list ->
                    list.map { entity ->
                        KidListItemUi(
                            id = entity.id,
                            name = entity.name.ifBlank { "未命名宝贝" },
                            subtitle = "点击【成长记录】或【乖不乖日历】查看数据",
                            avatarUri = entity.avatarUri
                        )
                    }
                }
                .collect { _kids.value = it }
        }
    }

    // 目前添加和编辑都在详情页中完成，这里暂不提供快速添加
}

