document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing calculator...');

    // Состояние приложения
    let currentOperation = 'add';
    let operations = {};
    let slaeMethods = {};

    // Загрузка операций при старте
    fetch('/api/operations')
        .then(response => response.json())
        .then(data => {
            operations = data;
            renderOperationSelector();
        });

    // Загрузка методов СЛАУ
    fetch('/api/slae-methods')
        .then(response => response.json())
        .then(data => {
            slaeMethods = data;
        });

    // Рендер радио-кнопок операций
    function renderOperationSelector() {
        const container = document.getElementById('operation-selector');
        if (!container) return;

        let html = '<div class="operation-group">';
        for (const [value, label] of Object.entries(operations)) {
            html += `
                <label class="operation-option">
                    <input type="radio" name="operation" value="${value}"
                           ${value === currentOperation ? 'checked' : ''}>
                    ${label}
                </label>
            `;
        }
        html += '</div>';
        container.innerHTML = html;

        document.querySelectorAll('input[name="operation"]').forEach(radio => {
            radio.addEventListener('change', onOperationChange);
        });

        updatePanelsVisibility();
    }

    function onOperationChange(e) {
        currentOperation = e.target.value;
        updatePanelsVisibility();

        // Для СЛАУ проверяем, что матрица квадратная ТОЛЬКО если выбран матричный метод или Крамера
        if (currentOperation === 'slae') {
            const method = document.getElementById('slae-method')?.value;
            const rowsA = parseInt(document.getElementById('rows-a').value);
            const colsA = parseInt(document.getElementById('cols-a').value);

            // Только для матричного метода и метода Крамера нужна квадратная матрица
            if ((method === 'matrix' || method === 'cramer') && rowsA !== colsA) {
                alert('Для матричного метода и метода Крамера матрица коэффициентов должна быть квадратной. Увеличьте количество столбцов.');
                document.getElementById('cols-a').value = rowsA;
                updateMatrixA();
            }
        }
    }

    function updatePanelsVisibility() {
        const matrixBPanel = document.getElementById('matrix-b-panel');
        const scalarPanel = document.getElementById('scalar-panel');
        const slaePanel = document.getElementById('slae-panel');
        const vectorBPanel = document.getElementById('vector-b-panel');

        const binaryOps = ['add', 'subtract', 'multiply'];
        const scalarOps = ['multiplyScalar'];

        // Обычные операции
        if (matrixBPanel) {
            matrixBPanel.style.display = binaryOps.includes(currentOperation) ? 'block' : 'none';
        }
        if (scalarPanel) {
            scalarPanel.style.display = scalarOps.includes(currentOperation) ? 'block' : 'none';
        }

        // СЛАУ
        const isSlae = currentOperation === 'slae';
        if (slaePanel) {
            slaePanel.style.display = isSlae ? 'block' : 'none';
        }
        if (vectorBPanel) {
            vectorBPanel.style.display = isSlae ? 'block' : 'none';
        }

        // Для СЛАУ скрываем обычную матрицу B
        if (matrixBPanel && isSlae) {
            matrixBPanel.style.display = 'none';
        }
    }

    // Сохранение значений матрицы в localStorage
    function saveMatrixValuesToStorage(prefix) {
        const cells = document.querySelectorAll(`.matrix-cell[data-matrix="${prefix}"]`);
        const values = {};

        cells.forEach(cell => {
            const row = cell.dataset.row;
            const col = cell.dataset.col;
            if (!values[row]) values[row] = {};
            values[row][col] = cell.value;
        });

        localStorage.setItem(`matrix_${prefix}`, JSON.stringify(values));
        return values;
    }

    // Загрузка значений матрицы из localStorage
    function loadMatrixValuesFromStorage(prefix) {
        const saved = localStorage.getItem(`matrix_${prefix}`);
        return saved ? JSON.parse(saved) : null;
    }

    // Создание HTML матрицы
    function createMatrixHTML(rows, cols, prefix) {
        const savedValues = loadMatrixValuesFromStorage(prefix);

        let html = '<table class="matrix">';
        for (let i = 0; i < rows; i++) {
            html += '<tr>';
            for (let j = 0; j < cols; j++) {
                let value = '0';
                if (savedValues && savedValues[i] && savedValues[i][j] !== undefined) {
                    value = savedValues[i][j];
                }
                // Добавляем id и name для устранения предупреждения
                const cellId = `${prefix}_cell_${i}_${j}`;
                const cellName = `${prefix}[${i}][${j}]`;
                html += `<td><input type="number" class="matrix-cell"
                           id="${cellId}"
                           name="${cellName}"
                           data-matrix="${prefix}" data-row="${i}" data-col="${j}"
                           value="${value}" step="any"></td>`;
            }
            html += '</tr>';
        }
        html += '</table>';
        return html;
    }

    // Создание HTML для вектора правых частей
    // Создание HTML для вектора правых частей с id и name
    function createVectorHTML(size) {
        let html = '<div class="vector-container">';
        for (let i = 0; i < size; i++) {
            const cellId = `vector_cell_${i}`;
            const cellName = `vector[${i}]`;
            html += `
                <div class="vector-input">
                    <label for="${cellId}">b<sub>${i+1}</sub>:</label>
                    <input type="number" class="vector-cell"
                           id="${cellId}"
                           name="${cellName}"
                           data-index="${i}" value="0" step="any">
                </div>
            `;
        }
        html += '</div>';
        return html;
    }

    // Обновление матрицы A
    function updateMatrixA() {
        const rows = parseInt(document.getElementById('rows-a').value);
        const cols = parseInt(document.getElementById('cols-a').value);

        saveMatrixValuesToStorage('A');

        const matrixDiv = document.getElementById('matrix-a');
        if (matrixDiv) {
            matrixDiv.innerHTML = createMatrixHTML(rows, cols, 'A');
        }

        const sizeSpan = document.getElementById('matrix-a-size');
        if (sizeSpan) {
            sizeSpan.textContent = `(${rows}×${cols})`;
        }

        // Если это СЛАУ, обновляем вектор
        if (currentOperation === 'slae') {
            updateVectorB();
        }
    }

    // Обновление матрицы B
    function updateMatrixB() {
        const rows = parseInt(document.getElementById('rows-b').value);
        const cols = parseInt(document.getElementById('cols-b').value);

        saveMatrixValuesToStorage('B');

        const matrixDiv = document.getElementById('matrix-b');
        if (matrixDiv) {
            matrixDiv.innerHTML = createMatrixHTML(rows, cols, 'B');
        }

        const sizeSpan = document.getElementById('matrix-b-size');
        if (sizeSpan) {
            sizeSpan.textContent = `(${rows}×${cols})`;
        }
    }

    // Обновление вектора B для СЛАУ
    function updateVectorB() {
        const size = parseInt(document.getElementById('rows-a').value);
        const vectorDiv = document.getElementById('vector-b');
        if (vectorDiv) {
            vectorDiv.innerHTML = createVectorHTML(size);
        }
    }

    // Получение значений вектора B
    function getVectorB() {
        const size = parseInt(document.getElementById('rows-a').value);
        const vector = [];
        for (let i = 0; i < size; i++) {
            const cell = document.querySelector(`.vector-cell[data-index="${i}"]`);
            vector.push(parseFloat(cell ? cell.value : 0) || 0);
        }
        return vector;
    }

    // Сбор значений матрицы
    function getMatrixValues(prefix) {
        const rows = prefix === 'A' ?
            parseInt(document.getElementById('rows-a').value) :
            parseInt(document.getElementById('rows-b').value);
        const cols = prefix === 'A' ?
            parseInt(document.getElementById('cols-a').value) :
            parseInt(document.getElementById('cols-b').value);

        const matrix = [];
        for (let i = 0; i < rows; i++) {
            const row = [];
            for (let j = 0; j < cols; j++) {
                const cell = document.querySelector(`.matrix-cell[data-matrix="${prefix}"][data-row="${i}"][data-col="${j}"]`);
                row.push(parseFloat(cell ? cell.value : 0) || 0);
            }
            matrix.push(row);
        }
        return matrix;
    }

    // Инициализация
    console.log('Initializing matrices...');
    updateMatrixA();
    updateMatrixB();
    updateVectorB();
    updatePanelsVisibility();

    // Обработчики кнопок
    document.getElementById('resize-a')?.addEventListener('click', updateMatrixA);
    document.getElementById('resize-b')?.addEventListener('click', updateMatrixB);

    // Обработчики Enter
    document.getElementById('rows-a')?.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') updateMatrixA();
    });
    document.getElementById('cols-a')?.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') updateMatrixA();
    });
    document.getElementById('rows-b')?.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') updateMatrixB();
    });
    document.getElementById('cols-b')?.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') updateMatrixB();
    });

    // Обработчик вычисления
    // Обработчик вычисления
    document.getElementById('calculate-btn')?.addEventListener('click', function() {
        const request = {
            operation: currentOperation
        };

        if (currentOperation === 'slae') {
            const method = document.getElementById('slae-method')?.value;
            const rowsA = parseInt(document.getElementById('rows-a').value);
            const colsA = parseInt(document.getElementById('cols-a').value);

            // Проверка для методов, требующих квадратную матрицу
            if ((method === 'matrix' || method === 'cramer') && rowsA !== colsA) {
                alert('Для матричного метода и метода Крамера матрица коэффициентов должна быть квадратной!');
                return;
            }

            request.matrixA = getMatrixValues('A');
            request.vectorB = getVectorB();
            request.slaeMethod = method || 'gauss';
        } else {
            request.matrixA = getMatrixValues('A');

            const binaryOps = ['add', 'subtract', 'multiply'];
            if (binaryOps.includes(currentOperation)) {
                request.matrixB = getMatrixValues('B');
            }

            if (currentOperation === 'multiplyScalar') {
                request.scalar = parseFloat(document.getElementById('scalar').value) || 1;
            }
        }

        console.log('Sending request:', request);

        fetch('/api/calculate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displayResult(data.data);
                if (data.steps && data.steps.length > 0) {
                    displaySteps(data.steps);
                    // Прокрутка к шагам
                    document.getElementById('steps-panel').scrollIntoView({
                        behavior: 'smooth',
                        block: 'nearest'
                    });
                }
                document.getElementById('error').style.display = 'none';
            } else {
                displayError(data.error);
            }
        });
    });

    function displayResult(data) {
        const resultDiv = document.getElementById('result');
        if (!resultDiv) return;

        if (typeof data === 'number') {
            const displayValue = Number.isInteger(data) ? data : data.toFixed(4);
            resultDiv.innerHTML = `<h3>Результат: <span class="number-result">${displayValue}</span></h3>`;
        }
        else if (Array.isArray(data)) {
            if (data.length > 0 && !Array.isArray(data[0])) {
                // Вектор (решение СЛАУ)
                let html = '<h3>Решение системы:</h3><div class="solution-vector">';
                for (let i = 0; i < data.length; i++) {
                    const val = data[i];
                    const displayVal = Number.isInteger(val) ? val : val.toFixed(4);
                    html += `<div class="solution-item">x<sub>${i+1}</sub> = ${displayVal}</div>`;
                }
                html += '</div>';
                resultDiv.innerHTML = html;
            } else {
                // Матрица
                let html = '<h3>Результат:</h3><table class="matrix result-matrix">';
                for (let i = 0; i < data.length; i++) {
                    html += '<tr>';
                    for (let j = 0; j < data[i].length; j++) {
                        const val = data[i][j];
                        const displayVal = Number.isInteger(val) ? val : val.toFixed(4);
                        html += `<td>${displayVal}</td>`;
                    }
                    html += '</tr>';
                }
                html += '</table>';
                resultDiv.innerHTML = html;
            }
        }
    }

    function displaySteps(steps) {
        console.log('Received steps:', steps); // ОТЛАДКА

        const stepsPanel = document.getElementById('steps-panel');
        const stepsList = document.getElementById('steps-list');

        if (!stepsPanel || !stepsList) return;

        let html = '';
        steps.forEach((step, index) => {
            console.log(`Step ${index}:`, step); // ОТЛАДКА

            html += '<div class="step-item">';

            // Текст шага
            if (step.text) {
                html += `<div class="step-text">${step.text}</div>`;
            }

            // Если есть матричные данные
            if (step.matrix && step.type) {
                console.log(`Step ${index} type: ${step.type}, matrix:`, step.matrix); // ОТЛАДКА

                if (step.type === 'matrix') {
                    html += formatMatrixHTML(step.matrix);
                } else if (step.type === 'vector') {
                    html += formatVectorHTML(step.matrix);
                } else if (step.type === 'augmented') {
                    html += formatAugmentedMatrixHTML(step.matrix);
                }
            }

            html += '</div>';
        });

        stepsList.innerHTML = html;
        stepsPanel.style.display = 'block';
    }

    function displayError(message) {
        document.getElementById('result').innerHTML = '';
        document.getElementById('steps-panel').style.display = 'none';
        
        const errorDiv = document.getElementById('error');
        if (errorDiv) {
            errorDiv.textContent = 'Ошибка: ' + message;
            errorDiv.style.display = 'block';
        }
    }

    // Функция для красивого отображения матрицы
    function formatMatrixHTML(matrix) {
        if (!matrix || !matrix.length) return '';

        const rows = matrix.length;
        const cols = matrix[0].length;

        let html = '<table class="steps-matrix">';
        for (let i = 0; i < rows; i++) {
            html += '<tr>';
            for (let j = 0; j < cols; j++) {
                const val = matrix[i][j];
                // Форматируем число
                let displayVal;
                if (Number.isInteger(val)) {
                    displayVal = val;
                } else {
                    displayVal = val.toFixed(2);
                }
                html += `<td class="steps-matrix-cell">${displayVal}</td>`;
            }
            html += '</tr>';
        }
        html += '</table>';
        return html;
    }

    // Функция для красивого отображения вектора
    function formatVectorHTML(vector) {
        if (!vector || !vector.length) return '';

        let html = '<div class="steps-vector">';
        html += '<table class="steps-matrix">';
        html += '<tr>';
        for (let i = 0; i < vector.length; i++) {
            const val = vector[i];
            let displayVal;
            if (Number.isInteger(val)) {
                displayVal = val;
            } else {
                displayVal = val.toFixed(2);
            }
            html += `<td class="steps-matrix-cell">${displayVal}</td>`;
        }
        html += '</tr>';
        html += '</table>';
        html += '</div>';
        return html;
    }

    // Функция для форматирования расширенной матрицы (с вертикальной чертой)
    function formatAugmentedMatrixHTML(matrix) {
        console.log('formatAugmentedMatrixHTML called with:', matrix);

        if (!matrix || !matrix.length) {
            console.log('Matrix is empty');
            return '';
        }

        const rows = matrix.length;
        const cols = matrix[0].length;
        console.log(`Matrix size: ${rows}x${cols}`);

        let html = '<table class="steps-matrix augmented">';
        for (let i = 0; i < rows; i++) {
            html += '<tr>';
            for (let j = 0; j < cols; j++) {
                const val = matrix[i][j];
                // Форматируем число
                let displayVal;
                if (Number.isInteger(val)) {
                    displayVal = val;
                } else {
                    displayVal = val.toFixed(2);
                }
                // Добавляем класс last-col для последнего столбца
                const cellClass = (j === cols - 1) ? 'steps-matrix-cell last-col' : 'steps-matrix-cell';
                html += `<td class="${cellClass}">${displayVal}</td>`;
            }
            html += '</tr>';
        }
        html += '</table>';
        return html;
    }
});