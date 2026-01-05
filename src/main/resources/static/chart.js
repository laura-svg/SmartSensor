fetch('/api/sensors/temperature/daily?start=2023-01-01T00:00:00&end=2023-01-02T00:00:00')
    .then(response => response.json())
    .then(data => {
        const ctx = document.getElementById('temperatureChart').getContext('2d');
        const temperatures = data.map(item => item.value);
        const timestamps = data.map(item => item.timestamp);

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: timestamps,
                datasets: [{
                    label: 'Temperature over time',
                    data: temperatures,
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1,
                    fill: false
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    });