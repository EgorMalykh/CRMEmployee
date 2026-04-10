import React, { useEffect, useState } from 'react';
import './App.css';
import ModalWindow from './components/modal_window/ModalWindow';
import SearchBar from './components/searchBar/SearchBar';

function App() {
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [patronymic, setPatronymic] = useState('');
    const [email, setEmail] = useState('');
    const [numberPhone, setNumberPhone] = useState('');
    const [department, setDepartment] = useState('');
    const [post, setPost] = useState('');

    const [modal, setModal] = useState(false);
    const [editModal, setEditModal] = useState(false);

    const [selectedEmployee, setSelectedEmployee] = useState(null);
    const [employees, setEmployees] = useState([]);

    const [loading, setLoading] = useState(false);

    const [searchTerm, setSearchTerm] = useState('');
    const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
    const [toasts, setToasts] = useState([]);

    const [formErrors, setFormErrors] = useState({});

    const [currentPage, setCurrentPage] = useState(1);
    const employeesPerPage = 5;
    const [totalPages, setTotalPages] = useState(1);

    const showToast = (message, type = 'info') => {
        const id = Date.now();
        setToasts(prev => [...prev, { id, message, type }]);
        setTimeout(() => {
            setToasts(prev => prev.filter(t => t.id !== id));
        }, 5000);
    };

    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedSearchTerm(searchTerm);
        }, 1000);

        return () => {
            clearTimeout(handler);
        };
    }, [searchTerm]);

    useEffect(() => {
        const fetchEmployees = () => {
            setLoading(true);
            const url = debouncedSearchTerm
                ? `http://localhost:8080/api/v1/employees/search?page=${currentPage - 1}&size=${employeesPerPage}&fullName=${encodeURIComponent(debouncedSearchTerm)}`
                : `http://localhost:8080/api/v1/employees?page=${currentPage - 1}&size=${employeesPerPage}`;

            fetch(url)
                .then((response) => {
                    if (!response.ok) throw new Error('Ошибка загрузки');
                    return response.json();
                })
                .then((data) => {
                    setEmployees(data.content || []);
                    setTotalPages(data.totalPages || 1);
                })
                .catch((err) => {
                    console.error('Ошибка при загрузке сотрудников:', err);
                    showToast('Не удалось загрузить данные', 'error');
                })
                .finally(() => setLoading(false));
        };

        fetchEmployees();
    }, [currentPage, debouncedSearchTerm]);

    useEffect(() => {
        setCurrentPage(1);
    }, [debouncedSearchTerm]);

    const resetForm = () => {
        setFirstName('');
        setLastName('');
        setPatronymic('');
        setEmail('');
        setNumberPhone('');
        setDepartment('');
        setPost('');
        setFormErrors({});
    };

    const parseBackendErrors = (errorText) => {
        try {
            const data = JSON.parse(errorText);
            const errors = {};

            if (data.field && data.message) {
                errors[data.field] = data.message;
            } else if (data.message && !data.message.includes('Ошибка загрузки')) {
                errors.general = data.message;
            }

            return errors;
        } catch (e) {
            console.warn("Не удалось распарсить ошибку:", errorText);
            return { general: 'Ошибка при заполнении формы. Проверьте данные.' };
        }
    };

    const addEmployeeHandler = (e) => {
        e.preventDefault();
        const patronymicForBackend = patronymic.trim() === '' ? null : patronymic;

        const requestBody = {
            firstName,
            lastName,
            patronymic: patronymicForBackend,
            email,
            numberPhone,
            department,
            post,
        };

        fetch('http://localhost:8080/api/v1/employees', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody),
        })
            .then(async (response) => {
                if (!response.ok) {
                    const text = await response.text();
                    throw new Error(text);
                }
                return response.json();
            })
            .then((newEmp) => {
                setEmployees(prev => [newEmp, ...prev]);
                resetForm();
                setModal(false);
                showToast('Сотрудник успешно добавлен', 'success');
            })
            .catch((err) => {
                console.error('Ошибка при добавлении сотрудника:', err.message);
                const errors = parseBackendErrors(err.message);
                setFormErrors(errors);
                showToast(errors.general || 'Ошибка в заполнении формы', 'error');
            });
    };

    const openEditModal = (employee) => {
        setSelectedEmployee(employee);
        setFirstName(employee.firstName);
        setLastName(employee.lastName);
        setPatronymic(employee.patronymic || '');
        setEmail(employee.email);
        setNumberPhone(employee.numberPhone);
        setDepartment(employee.department);
        setPost(employee.post);
        setFormErrors({});
        setEditModal(true);
    };

    const updateEmployeeHandler = (e) => {
        e.preventDefault();
        const patronymicForBackend = patronymic.trim() === '' ? null : patronymic;

        const requestBody = {
            firstName,
            lastName,
            patronymic: patronymicForBackend,
            email,
            numberPhone,
            department,
            post,
        };

        fetch(`http://localhost:8080/api/v1/employees/${selectedEmployee.id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody),
        })
            .then(async (response) => {
                if (!response.ok) {
                    const text = await response.text();
                    throw new Error(text);
                }
                return response.json();
            })
            .then((updatedEmp) => {
                setEmployees(prev => prev.map(emp => emp.id === updatedEmp.id ? updatedEmp : emp));
                setEditModal(false);
                setSelectedEmployee(null);
                resetForm();
                showToast('Сотрудник успешно обновлён', 'success');
            })
            .catch((err) => {
                console.error('Ошибка при обновлении сотрудника:', err.message);
                const errors = parseBackendErrors(err.message);
                setFormErrors(errors);
                showToast(errors.general || 'Ошибка в заполнении формы', 'error');
            });
    };

    const deleteEmployee = () => {
        if (!window.confirm('Вы уверены, что хотите удалить этого сотрудника?')) return;

        fetch(`http://localhost:8080/api/v1/employees/${selectedEmployee.id}`, {
            method: 'DELETE',
        })
            .then((response) => {
                if (!response.ok) {
                    return response.text().then(text => { throw new Error(text) });
                }
                setEmployees(prev => prev.filter(emp => emp.id !== selectedEmployee.id));
                setEditModal(false);
                setSelectedEmployee(null);
                resetForm();
                showToast('Сотрудник успешно удалён', 'success');
            })
            .catch((err) => {
                console.error('Ошибка при удалении сотрудника:', err);
                showToast('Не удалось удалить сотрудника', 'error');
            });
    };

    const paginate = (pageNumber) => setCurrentPage(pageNumber);
    const nextPage = () => setCurrentPage(prev => Math.min(prev + 1, totalPages));
    const prevPage = () => setCurrentPage(prev => Math.max(prev - 1, 1));

    const currentEmployees = employees;

    return (
        <div className="bg-gradient-to-br from-blue-50 via-white to-indigo-50 min-h-screen relative">
            <div className="fixed top-4 right-4 z-50 space-y-2">
                {toasts.map((toast) => (
                    <div
                        key={toast.id}
                        className={`p-4 rounded-xl shadow-lg text-white max-w-xs transform transition-all duration-300 ${
                            toast.type === 'error'
                                ? 'bg-red-500'
                                : toast.type === 'success'
                                ? 'bg-green-500'
                                : 'bg-blue-500'
                        }`}
                    >
                        {toast.message}
                    </div>
                ))}
            </div>

            {loading ? (
                <div className="flex flex-col items-center justify-center h-screen">
                    <div className="w-16 h-16 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
                    <div className="mt-4 text-xl text-gray-600">Загрузка...</div>
                </div>
            ) : (
                <div className="p-6 max-w-6xl mx-auto">
                    <div className="flex items-center gap-3 mb-2 justify-center">
                        <div className="flex items-center justify-center w-10 h-10 bg-gradient-to-br from-blue-600 to-indigo-700 rounded-full text-white font-bold text-lg shadow-md">
                            TNS
                        </div>
                        <h2 className="text-xl font-semibold text-gray-700">TechNova Solutions</h2>
                    </div>


                    <h1 className="flex items-center gap-3 text-4xl font-bold text-gray-800 mb-6 text-center">
                        <span className="text-blue-600 text-5xl">👩👨</span>
                        Сотрудники
                    </h1>

                    <SearchBar searchTerm={searchTerm} onSearch={setSearchTerm} />


                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 mt-6">
                        {currentEmployees.length > 0 ? (
                            currentEmployees.map((emp) => (
                                <div
                                    key={emp.id}
                                    onClick={() => openEditModal(emp)}
                                    className="bg-white p-6 rounded-xl shadow-md hover:shadow-xl transition-all duration-300 cursor-pointer transform hover:-translate-y-1 border border-gray-100"
                                >
                                    <h3 className="text-xl font-semibold text-gray-800 mb-2">
                                        {emp.lastName} {emp.firstName} {emp.patronymic || ''}
                                    </h3>
                                    <p className="text-gray-600 mb-1 flex items-center gap-1">
                                        ✉️ {emp.email}
                                    </p>
                                    <p className="text-gray-600 mb-1 flex items-center gap-1">
                                        📞 {emp.numberPhone}
                                    </p>
                                    <p className="text-gray-500 text-sm">
                                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                                            emp.department === 'IT' ? 'bg-blue-100 text-blue-800' :
                                            emp.department === 'HR' ? 'bg-green-100 text-green-800' :
                                            emp.department === 'FINANCE' ? 'bg-yellow-100 text-yellow-800' :
                                            emp.department === "SUPPORT" ? 'bg-red-100 text-red-800' :
                                            emp.department ==='LEGAL' ? 'bg-pink-100 text-pink-800' :
                                            'bg-purple-100 text-purple-800'
                                        }`}>
                                            {emp.department}
                                        </span>
                                    </p>
                                    <p className="text-gray-700 mt-2 font-medium">{emp.post}</p>
                                </div>
                            ))
                        ) : (
                            <div className="col-span-full flex flex-col items-center justify-center py-10 text-gray-500">
                                <span className="text-6xl mb-2">🔍</span>
                                <p className="text-lg">Сотрудники не найдены</p>
                            </div>
                        )}
                    </div>


                    <div className="flex items-center justify-between p-4 bg-white rounded-lg shadow mt-6">
                        <button
                            onClick={prevPage}
                            disabled={currentPage === 1}
                            className={`px-5 py-2 rounded-lg ${
                                currentPage === 1
                                    ? 'text-gray-400 cursor-not-allowed'
                                    : 'text-blue-600 hover:bg-blue-50'
                            } transition`}
                        >
                            ← Назад
                        </button>
                        <div className="flex space-x-2">
                            {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
                                <button
                                    key={page}
                                    onClick={() => paginate(page)}
                                    className={`w-10 h-10 rounded-full transition ${
                                        currentPage === page
                                            ? 'bg-blue-600 text-white'
                                            : 'text-gray-700 hover:bg-gray-200'
                                    }`}
                                >
                                    {page}
                                </button>
                            ))}
                        </div>
                        <button
                            onClick={nextPage}
                            disabled={currentPage === totalPages}
                            className={`px-5 py-2 rounded-lg ${
                                currentPage === totalPages
                                    ? 'text-gray-400 cursor-not-allowed'
                                    : 'text-blue-600 hover:bg-blue-50'
                            } transition`}
                        >
                            Вперёд →
                        </button>
                    </div>


                    <div className="mt-6 text-right">
                        <button
                            onClick={() => {
                                setModal(true);
                                setFormErrors({});
                            }}
                            className="inline-flex items-center px-6 py-3 bg-gradient-to-r from-blue-600 to-indigo-700 text-white font-semibold rounded-xl shadow-lg hover:from-blue-700 hover:to-indigo-800 focus:outline-none focus:ring-4 focus:ring-blue-300 transition-all duration-200 transform hover:scale-105"
                        >
                            ➕ Добавить сотрудника
                        </button>
                    </div>
                </div>
            )}


            <ModalWindow open={modal}>
                <div className="w-[520px] h-auto p-8 bg-white rounded-2xl shadow-2xl">
                    <h2 className="text-center text-3xl font-bold text-gray-800 mb-6">➕ Добавить сотрудника</h2>
                    <form onSubmit={addEmployeeHandler}>
                        {[
                            { label: 'Имя', name: 'firstName', value: firstName, setter: setFirstName },
                            { label: 'Фамилия', name: 'lastName', value: lastName, setter: setLastName },
                            { label: 'Отчество', name: 'patronymic', value: patronymic, setter: setPatronymic },
                            { label: 'Почта', name: 'email', value: email, setter: setEmail, type: 'email' },
                            { label: 'Телефон', name: 'numberPhone', value: numberPhone, setter: setNumberPhone, type: 'tel' },
                            { label: 'Должность', name: 'post', value: post, setter: setPost },
                        ].map((field) => (
                            <div className="mb-5" key={field.name}>
                                <label className="block text-lg font-medium text-gray-700 mb-2" htmlFor={field.name}>
                                    {field.label}
                                </label>
                                <input
                                    className={`w-full px-4 py-3 text-base border-2 rounded-lg focus:outline-none transition-colors duration-200 ${
                                        formErrors[field.name]
                                            ? 'border-red-400 focus:border-red-500'
                                            : 'border-gray-300 focus:border-blue-500'
                                    }`}
                                    type={field.type || 'text'}
                                    name={field.name}
                                    id={field.name}
                                    value={field.value}
                                    onChange={(e) => {
                                        field.setter(e.target.value);
                                        setFormErrors(prev => ({ ...prev, [field.name]: undefined }));
                                    }}
                                />
                                {formErrors[field.name] && (
                                    <p className="text-red-500 text-sm mt-1 flex items-center gap-1">
                                        ⚠️ {formErrors[field.name]}
                                    </p>
                                )}
                            </div>
                        ))}

                        <div className="mb-5">
                            <label className="block text-lg font-medium text-gray-700 mb-2">Отдел</label>
                            <div className="grid grid-cols-2 gap-3 mt-2">
                                {['IT', 'HR', 'FINANCE', 'MARKETING', 'LEGAL', 'SUPPORT'].map((dept) => (
                                    <label key={dept} className="flex items-center space-x-2 cursor-pointer">
                                        <input
                                            type="radio"
                                            name="department"
                                            value={dept}
                                            checked={department === dept}
                                            onChange={(e) => {
                                                setDepartment(e.target.value);
                                                setFormErrors(prev => ({ ...prev, department: undefined }));
                                            }}
                                            className="w-5 h-5 text-blue-600 border-gray-300 focus:ring-blue-500"
                                        />
                                        <span className={`text-gray-700 font-medium ${formErrors.department ? 'text-red-700' : ''}`}>
                                            {dept}
                                        </span>
                                    </label>
                                ))}
                            </div>
                            {formErrors.department && <p className="text-red-500 text-sm mt-1">⚠️ {formErrors.department}</p>}
                        </div>

                        <div className="flex justify-end gap-3 mt-6">
                            <button
                                type="button"
                                onClick={() => {
                                    setModal(false);
                                    resetForm();
                                }}
                                className="px-6 py-2 bg-gray-400 text-white rounded-lg hover:bg-gray-500 transition"
                            >
                                Отмена
                            </button>
                            <button
                                type="submit"
                                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                            >
                                Добавить
                            </button>
                        </div>
                    </form>
                </div>
            </ModalWindow>


            <ModalWindow open={editModal}>
                <div className="w-[520px] h-auto p-8 bg-white rounded-2xl shadow-2xl">
                    <h2 className="text-center text-3xl font-bold text-gray-800 mb-6">✏️ Редактировать сотрудника</h2>
                    <form onSubmit={updateEmployeeHandler}>
                        {[
                            { label: 'Имя', name: 'firstName', value: firstName, setter: setFirstName },
                            { label: 'Фамилия', name: 'lastName', value: lastName, setter: setLastName },
                            { label: 'Отчество', name: 'patronymic', value: patronymic, setter: setPatronymic },
                            { label: 'Почта', name: 'email', value: email, setter: setEmail, type: 'email' },
                            { label: 'Телефон', name: 'numberPhone', value: numberPhone, setter: setNumberPhone, type: 'tel' },
                            { label: 'Должность', name: 'post', value: post, setter: setPost },
                        ].map((field) => (
                            <div className="mb-5" key={field.name}>
                                <label className="block text-lg font-medium text-gray-700 mb-2" htmlFor={field.name}>
                                    {field.label}
                                </label>
                                <input
                                    className={`w-full px-4 py-3 text-base border-2 rounded-lg focus:outline-none transition-colors duration-200 ${
                                        formErrors[field.name]
                                            ? 'border-red-400 focus:border-red-500'
                                            : 'border-gray-300 focus:border-blue-500'
                                    }`}
                                    type={field.type || 'text'}
                                    name={field.name}
                                    id={field.name}
                                    value={field.value}
                                    onChange={(e) => {
                                        field.setter(e.target.value);
                                        setFormErrors(prev => ({ ...prev, [field.name]: undefined }));
                                    }}
                                />
                                {formErrors[field.name] && (
                                    <p className="text-red-500 text-sm mt-1 flex items-center gap-1">
                                        ⚠️ {formErrors[field.name]}
                                    </p>
                                )}
                            </div>
                        ))}

                        <div className="mb-5">
                            <label className="block text-lg font-medium text-gray-700 mb-2">Отдел</label>
                            <div className="grid grid-cols-2 gap-3 mt-2">
                                {['IT', 'HR', 'FINANCE', 'MARKETING', 'LEGAL', 'SUPPORT'].map((dept) => (
                                    <label key={dept} className="flex items-center space-x-2 cursor-pointer">
                                        <input
                                            type="radio"
                                            name="department"
                                            value={dept}
                                            checked={department === dept}
                                            onChange={(e) => {
                                                setDepartment(e.target.value);
                                                setFormErrors(prev => ({ ...prev, department: undefined }));
                                            }}
                                            className="w-5 h-5 text-blue-600 border-gray-300 focus:ring-blue-500"
                                        />
                                        <span className={`text-gray-700 font-medium ${formErrors.department ? 'text-red-700' : ''}`}>
                                            {dept}
                                        </span>
                                    </label>
                                ))}
                            </div>
                            {formErrors.department && <p className="text-red-500 text-sm mt-1">⚠️ {formErrors.department}</p>}
                        </div>

                        <div className="flex justify-between gap-3 mt-6">
                            <button
                                type="button"
                                onClick={deleteEmployee}
                                className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
                            >
                                Удалить
                            </button>
                            <div className="flex gap-3">
                                <button
                                    type="button"
                                    onClick={() => {
                                        setEditModal(false);
                                        resetForm();
                                    }}
                                    className="px-6 py-2 bg-gray-400 text-white rounded-lg hover:bg-gray-500 transition"
                                >
                                    Отмена
                                </button>
                                <button
                                    type="submit"
                                    className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                                >
                                    Сохранить
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </ModalWindow>
        </div>
    );
}

export default App;