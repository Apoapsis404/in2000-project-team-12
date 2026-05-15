import concurrent.futures
import requests
import time

client_id='7e3f32d8-a66e-45a7-b8be-14e67bfbd5d4'
client_secret='4f33a622-b2de-4ca1-8dd0-2941e8d33a4a'
        
# 1. Configuration
CLIENT_ID = 'YOUR_CLIENT_ID_HERE' # Get one at frost.met.no
STATION_ID = 'SN18700'            # Example: Oslo - Blindern
TIME_RANGE = '2023-03-01/2024-03-01' # Example (Storm Hans dates)
ELEMENT = 'sum(precipitation_amount PT10M)'

CURRENT_YEAR: int = 2026
CURRENT_MONTH: int = 5
CURRENT = "2026-05-01"
MAX_NUM_MONTHS = 120

def construct_time_series(n, current = CURRENT):
    return f"{last_n_months(n)}/{current}"

def last_n_months(n: int) -> str:
    current_year = CURRENT_YEAR
    current_month = CURRENT_MONTH

    # Calculate month (1-12)
    # Using (current - n - 1) % 12 + 1 handles the 0-index wrap properly
    from_month = (current_month - n - 1) % 12 + 1

    # Calculate year offset
    years_to_subtract = n // 12
    if current_month - (n % 12) <= 0:
        years_to_subtract = (n - current_month) // 12 + 1

    from_year = current_year - years_to_subtract
    
    return f"{from_year}-{from_month:02d}-01"


def months_since(date):
    s = date.split('-')
    year = int(s[0])
    month = int(s[1])
    return (CURRENT_YEAR - year) * 12 + (CURRENT_MONTH - month)
    

def years_since(date):
    return months_since(date) // 12
 
def get_time_series(source, element):
    endpoint = 'https://frost.met.no/observations/availableTimeSeries/v0.jsonld'
    params = {
        'sources': source,
        'elements': element,
    }
    
    # Requesting data (Client ID is used as the username, password is empty)
    r = requests.get(endpoint, params, auth=(client_id, client_secret))
    
    if r.status_code == 200:
        
        print("Query time: ", r.json()['queryTime'], "s")
        return r.json()['data']
    else:
        print(f"Error! Status code: {r.status_code}")
        print(f"Server says: {r.text}") # This will tell you the ACTUAL problem
        return None
    

def get_frost_data(source, time_range, element):
    endpoint = 'https://frost.met.no/observations/v0.jsonld'
    params = {
        'sources': source,
        'elements': element,
        'referencetime': time_range,
    }
    
    # Requesting data (Client ID is used as the username, password is empty)
    r = requests.get(endpoint, params, auth=(client_id, client_secret))
    
    if r.status_code == 200:
        print(f"{source}: Query time: {r.json()['queryTime']}s")
        return r.json()['data']
    else:
        print(f"Error! Status code: {r.status_code}")
        # print(f"Server says: {r.text}") # This will tell you the ACTUAL problem
        return None

def analyze_rain(data):
    TORRENTIAL_LIMIT_10M = 20 #mm
    
    found_events = 0
    
    for observation in data:
        val = observation['observations'][0]['value']
        
        if val >= TORRENTIAL_LIMIT_10M:
            found_events += 1
            

    return found_events

def get_met_stations(element):
    endpoint = 'https://frost.met.no/sources/v0.jsonld'
    params = {
        # 'ids': "SN59450, SN18700, SN87110, SN40250, SN5350, SN80740, SN26990, SN26900, SN83710, SN85890",
        'elements': element,
        'types': "SensorSystem",
        'stationholder': "MET.NO"
    }
    
    # Requesting data (Client ID is used as the username, password is empty)
    r = requests.get(endpoint, params, auth=(client_id, client_secret))
    
    if r.status_code == 200:
        print("Query time: ", r.json()['queryTime'], "s")
        return r.json()['data']
    else:
        print(f"Error! Status code: {r.status_code}")
        return None

    
def get_rain_events_from_stations():
    rain_element = "sum(precipitation_amount PT1H)"
    
    sources = get_met_stations(rain_element)
    res = []
    def process_station(source):
        sourceName = source['id']
        raw_data = get_frost_data(sourceName, construct_time_series(120), rain_element)
        
        if raw_data == None:
            return 

        events = analyze_rain(raw_data)

        print(f"Found {events} events for {sourceName}")
        return (source['geometry']['coordinates'], events) if events >= 3 else None

    res = []
    max_workers = min(5, len(sources))
    with concurrent.futures.ThreadPoolExecutor(max_workers=max_workers) as executor:
        for station_result in executor.map(process_station, sources):
            if station_result is not None:
                res.append(station_result)

    return res


def analyze_wind(data):
    STRONG_WIND = 17.2 #m/s source: https://www.hastings.gov.uk/content/weather-management/Beaufort_Wind_Scale.pdf
    
    found_events = 0
    
    for observation in data:
        val = observation['observations'][0]['value']
        
        if val >= STRONG_WIND:
            found_events += 1

    return found_events


def get_wind_events_from_stations():
    wind_element = "max(wind_speed P1D)"

    sources = get_met_stations(wind_element)
    res = []
    def process_station(source):
        sourceName = source['id']
        events = 0
        raw_data = get_frost_data(sourceName, construct_time_series(120), wind_element)
        if raw_data is None:
            return
        events += analyze_wind(raw_data)
        
        years = max(years_since(raw_data[0]['referenceTime']), 1)

        print(f"Found {events} events for {sourceName}")
        return (source['geometry']['coordinates'], events // years) if events / years >= 5 else None

    res = []
    max_workers = min(5, len(sources))
    with concurrent.futures.ThreadPoolExecutor(max_workers=max_workers) as executor:
        for station_result in executor.map(process_station, sources):
            if station_result is not None:
                res.append(station_result)

    return res


# Start the timer
start_time = time.perf_counter()

# # Run it
# print([x['id'] for x in get_met_stations("sum(precipitation_amount PT1H)")]) 
print(get_rain_events_from_stations())
# print(get_wind_events_from_stations())



end_time = time.perf_counter()

# Calculate and print duration
execution_time = end_time - start_time
print(f"Executed in {execution_time:.4f} seconds")

"""
Gale List
[([5.2007, 61.6788], 56), ([11.3475, 78.9633], 13), ([16.1312, 69.3073], 12), ([13.646, 66.9035], 9), ([6.7108, 59.8357], 75), ([12.1038, 67.5267], 29), ([5.268, 62.3293], 79), ([13.9067, 66.8303], 24), ([6.2877, 62.16], 8), ([7.8173, 59.7675], 77), ([7.9957, 61.5648], 6), ([13.6192, 78.0625], 22), ([7.4805, 60.1833], 16), ([14.1679, 67.094383], 50), ([18.9978, 74.5035], 18), ([6.8717, 62.5158], 24), ([10.148, 58.9688], 15), ([10.5493, 64.7977], 73), ([6.40585, 60.223533], 50), ([5.4122, 59.0705], 13), ([9.392, 62.6352], 32), ([6.8817, 59.3335], 27), ([20.1518, 69.5598], 14), ([15.943, 68.368], 36), ([6.1065, 61.8902], 78), ([4.715, 60.7525], 41), ([19.4997, 70.2452], 22), ([13.61214, 68.1557873], 6), ([5.0668, 59.907], 18), ([18.77, 78.035], 52), ([10.12662056, 61.46474255], 11), ([16.2433, 80.0555], 24), ([23.4403, 69.2187], 38), ([8.7893, 58.3988], 20), ([17.4802, 68.421], 79), ([29.1003, 70.306], 14), ([22.8177, 70.4107], 33), ([10.82829919, 59.15154567], 10), ([5.5545609, 58.6585044], 31), ([12.611, 66.3705], 22), ([6.5675, 58.109], 16), ([10.5242, 59.0272], 17), ([22.3413, 70.1728], 26), ([14.6485, 68.1535], 21), ([9.352, 62.2958], 68), ([22.8225, 78.2508], 14), ([15.378, 78.2278], 29), ([7.9482, 63.5176], 47), ([23.6772, 70.6807], 9), ([22.4428, 70.607], 105), ([10.9965, 65.2023], 67), ([5.87183, 58.43517], 52), ([20.5795, 69.2235], 37), ([6.849, 62.3065], 30), ([12.486, 66.7628], 45), ([14.7578, 77.6873], 23), ([28.9648, 70.3357], 31), ([31.4585, 80.1035], 44), ([8.0532, 58.0732], 9), ([20.11451, 70.10426], 69), ([11.8562, 65.7017], 5), ([18.2248, 78.3002], 57), ([7.2143, 59.8118], 9), ([6.096, 61.4573], 50), ([20.431, 70.075], 29), ([17.83117, 69.6005], 31), ([13.8958, 67.4048], 34), ([12.1887, 61.3188], 77), ([30.368, 70.2467], 31), ([31.0962, 70.3707], 10), ([4.9865, 62.034], 116), ([28.9937, 69.8043], 29), ([9.4052, 64.1727], 52), ([-8.669, 70.9394], 28), ([16.003, 69.2413], 58), ([11.9263, 78.6568], 14), ([28.217, 71.0888], 31), ([11.9928, 65.2193], 97), ([19.5442, 69.1025], 22), ([25.346, 69.3003], 41), ([7.925, 62.93], 81), ([27.8298, 71.033], 18), ([25.0048, 80.6518], 17), ([28.834, 78.918], 18), ([13.4483, 65.3143], 53), ([14.4347, 68.6072], 18), ([4.8723, 59.3065], 53), ([4.6817, 61.5717], 49), ([10.578, 59.4352], 6), ([15.3112, 68.7183], 76), ([6.4347, 60.2263], 26), ([25.9782, 71.0105], 18), ([16.3608, 68.5863], 39), ([17.7905, 68.1905], 9), ([14.3826294, 67.2726458], 12), ([18.1288, 69.2897], 64), ([6.5378, 62.8585], 60), ([7.527, 60.5938], 20), ([9.2862, 62.1133], 7), ([28.3773, 69.9363], 36), ([8.6562, 59.8497], 16), ([8.1785, 59.8397], 5), ([16.5428, 76.4733], 64), ([13.948, 66.8275], 9), ([18.6208, 69.4229], 16), ([5.078, 59.4382], 19), ([8.4543, 63.8429], 35), ([7.3892, 61.5972], 27), ([7.048, 57.9815], 40), ([30.07, 70.7057], 29), ([6.4675, 60.8603], 23), ([18.6938, 69.9903], 29), ([18.998, 74.5035], 18), ([9.6104, 63.7044], 15), ([6.8112, 58.9112], 50), ([30.0648, 70.3222], 5), ([23.9837577, 71.094447], 56), ([10.455, 64.4013], 50), ([11.9325, 78.9218], 7), ([16.4035, 78.18098], 13), ([8.369, 61.6775], 101)]

Torrential Rain List

[([10.72, 59.9423], 3), ([20.0930685, 69.5887], 3), ([10.345751, 59.229738], 4), ([6.270611, 59.4812702], 3), ([6.5046, 58.5072], 9), ([8.1999004, 59.4965232], 3), ([10.7818, 59.6605], 3), ([10.798, 59.3742], 3), ([10.82829919, 59.15154567], 3), ([6.5675, 58.109], 4), ([15.378, 78.2278], 13), ([6.4733, 60.8655], 5), ([8.0767, 58.2], 5), ([29.2427215, 69.14757], 3), ([4.8723, 59.3065], 3), ([6.4347, 60.2263], 5), ([10.6878682, 60.0514243], 4), ([7.4128, 58.6172], 4)]

"""