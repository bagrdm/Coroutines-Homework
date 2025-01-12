package otus.homework.coroutines

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CatsViewModel(
    private val catsFactService: CatsFactService,
    private val catsImageService: CatsImageService
) : ViewModel() {

    private val _state = MutableLiveData<Result<CatModel>>()
    val state: LiveData<Result<CatModel>> get() = _state

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        CrashMonitor.trackWarning()
        _state.value = throwable.message?.let { Result.Error(it) }
    }

    fun fetchData() {
        viewModelScope.launch(exceptionHandler) {
            val fact = async { catsFactService.getCatFact() }
            val image = async { catsImageService.getCatImage().first() }

            _state.value = Result.Success(
                CatModel(
                    fact.await(), image.await()
                )
            )
        }
    }

    init {
        fetchData()
    }
}

class CatsViewModelFactory(
    private val catsFactService: CatsFactService,
    private val catsImageService: CatsImageService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CatsViewModel(catsFactService, catsImageService) as T
    }
}
