import React from 'react';

const SearchBar = ({ searchTerm, onSearch }) => {
    return (
        <div className="relative mb-6">
            <input
                type="text"
                value={searchTerm}
                onChange={(e) => onSearch(e.target.value)}
                placeholder="Найти сотрудника..."
                className="w-full px-4 py-3 pl-10 text-lg text-gray-700 bg-white border-b-2 border-gray-300 focus:outline-none focus:border-blue-500 focus:bg-blue-50 transition-all duration-200 ease-in-out shadow-sm hover:shadow-md rounded-4xl"
            />
            <svg
                xmlns="http://www.w3.org/2000/svg"
                className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
            >
                <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
            </svg>
        </div>
    );
};

export default SearchBar;