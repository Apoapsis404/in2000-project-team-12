import concurrent.futures
import requests
import pandas as pd
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
 
print(months_since("2016-01-01"))
print(years_since("2016-01-01"))
print(last_n_months(124))

def get_time_series(source):
    endpoint = 'https://frost.met.no/observations/availableTimeSeries/v0.jsonld'
    params = {
        'sources': source,
        'elements': ELEMENT,
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
    

def get_frost_data(source, time_range):
    endpoint = 'https://frost.met.no/observations/v0.jsonld'
    params = {
        'sources': source,
        'elements': ELEMENT,
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
    # Threshold: 50mm/h. For a 10min period, this is 50 / 6 = 8.33mm
    TORRENTIAL_LIMIT_10M = 7.5 #mm
    
    found_events = 0
    
    for observation in data:
        time = observation['referenceTime']
        # The 'value' is the mm amount recorded in those 10 minutes
        val = observation['observations'][0]['value']
        
        # Calculate hourly rate for reporting
        hourly_rate = val * 6 
        
        if val >= TORRENTIAL_LIMIT_10M:
            # print(f"⚠️  TORRENTIAL RAIN: {time}")
            # print(f"   Amount: {val} mm in 10 mins | Rate: {hourly_rate:.2f} mm/h\n")
            found_events += 1
            

    return found_events

def get_met_stations():
    endpoint = 'https://frost.met.no/sources/v0.jsonld'
    params = {
        # 'ids': "SN18700,SN18500",
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
        # print(f"Server says: {r.text}") # This will tell you the ACTUAL problem
        return None
    
    
def get_rain_events_from_stations():
    # List of sourceIds
    sources = get_met_stations()
    # print(sources[0]['geometry']['coordinates'])
    # return
    res = []
    def process_station(source):
        sourceName = source['id']
        print(f"Trying to find data for station: {sourceName}")
        timeSeries = get_time_series(sourceName)
        if timeSeries is None:
            return None

        print(f"Found time series: {timeSeries[0]['validFrom']}")

        months = min(years_since(timeSeries[0]['validFrom']) * 12, 60)
        events = 0
        for i in range(0, months, 12):
            raw_data = get_frost_data(sourceName, construct_time_series(i + 12, last_n_months(i)))
            if raw_data is None:
                continue

            events += analyze_rain(raw_data)

        print(f"Found {events} events for {sourceName}")
        return (source['geometry']['coordinates'], events) if events > 5 else None

    res = []
    max_workers = min(5, len(sources))
    with concurrent.futures.ThreadPoolExecutor(max_workers=max_workers) as executor:
        for station_result in executor.map(process_station, sources):
            if station_result is not None:
                res.append(station_result)

    return res

print(get_rain_events_from_stations())



# timeSeries = get_time_series()
# if timeSeries:
#     print("time series: ", timeSeries[0]['validFrom'])
#     print(months_since(timeSeries[0]['validFrom']))



# # Start the timer
# start_time = time.perf_counter()

# # Run it
# raw_data = get_frost_data()

# # Got Data!

# end_time = time.perf_counter()

# # Calculate and print duration
# execution_time = end_time - start_time
# print(f"Executed in {execution_time:.4f} seconds")

# # timeSeries = get_time_series()
# # if timeSeries:
# #     print(timeSeries)


# if raw_data:
#     events = analyze_rain(raw_data)
#     print(events)

# # End the timer
# end_time = time.perf_counter()

# # Calculate and print duration
# execution_time = end_time - start_time
# print(f"Executed in {execution_time:.4f} seconds")