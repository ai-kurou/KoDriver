package kurou.kodriver.data

import kurou.kodriver.data.datasource.createConsoleAddressDataStore
import kurou.kodriver.data.repository.ConsoleAddressRepositoryImpl
import kurou.kodriver.domain.repository.ConsoleAddressRepository

fun createConsoleAddressRepository(directory: String): ConsoleAddressRepository =
    ConsoleAddressRepositoryImpl(createConsoleAddressDataStore(directory))
