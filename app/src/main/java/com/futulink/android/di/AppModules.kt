package com.futulink.android.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.futulink.android.BuildConfig
import com.futulink.android.data.local.datastore.ConfigLocalDataSource
import com.futulink.android.data.local.datastore.configDataStore
import com.futulink.android.data.local.room.FutuLinkDatabase
import com.futulink.android.data.local.room.MeasurementDao
import com.futulink.android.data.remote.ConfigRemoteDataSource
import com.futulink.android.data.remote.configureFutuLinkClient
import com.futulink.android.data.repository.ConfigRepositoryImpl
import com.futulink.android.data.repository.MeasurementRepositoryImpl
import com.futulink.android.data.repository.SpeedTestRepositoryImpl
import com.futulink.android.domain.repository.ConfigRepository
import com.futulink.android.domain.repository.MeasurementRepository
import com.futulink.android.domain.repository.SpeedTestRepository
import com.futulink.android.domain.usecase.ResolveStartupModeUseCase
import com.futulink.android.domain.usecase.SaveMeasurementUseCase
import com.futulink.android.presentation.speed.SpeedTestViewModel
import com.futulink.android.presentation.startup.StartupViewModel
import com.futulink.android.presentation.statistics.StatisticsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val networkModule = module {
    // One client for the whole application: creating an engine per test would waste
    // connections and thread pools.
    //
    // The OkHttp engine is used instead of Ktor's Android engine on purpose. The Android
    // engine closes the response stream from the thread that cancels the request, and the
    // platform HttpURLConnection stack then throws "Unbalanced enter/exit" from its own
    // timeout bookkeeping. That exception escapes a coroutine completion handler and crashes
    // the process on every Stop / tab switch of a streaming download. OkHttp cancels a
    // streaming call safely, which the speed test depends on.
    single { HttpClient(OkHttp) { configureFutuLinkClient() } }

    single {
        ConfigRemoteDataSource(
            httpClient = get(),
            configUrl = BuildConfig.REMOTE_CONFIG_URL,
        )
    }
}

val storageModule = module {
    single<DataStore<Preferences>> { androidContext().configDataStore }
    single { ConfigLocalDataSource(dataStore = get()) }

    single {
        Room.databaseBuilder(
            androidContext(),
            FutuLinkDatabase::class.java,
            FutuLinkDatabase.NAME,
        ).build()
    }
    single<MeasurementDao> { get<FutuLinkDatabase>().measurementDao() }
}

val repositoryModule = module {
    single<ConfigRepository> {
        ConfigRepositoryImpl(localDataSource = get(), remoteDataSource = get())
    }
    single<MeasurementRepository> { MeasurementRepositoryImpl(measurementDao = get()) }
    single<SpeedTestRepository> {
        SpeedTestRepositoryImpl(httpClient = get(), downloadUrl = BuildConfig.SPEED_TEST_URL)
    }
}

val useCaseModule = module {
    factory { ResolveStartupModeUseCase(configRepository = get()) }
    factory { SaveMeasurementUseCase(measurementRepository = get()) }
}

val viewModelModule = module {
    viewModel { StartupViewModel(resolveStartupMode = get()) }
    viewModel { SpeedTestViewModel(speedTestRepository = get(), saveMeasurement = get()) }
    viewModel { StatisticsViewModel(measurementRepository = get()) }
}

val appModules = listOf(
    networkModule,
    storageModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
)
