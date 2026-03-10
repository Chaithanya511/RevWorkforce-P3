import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { LeaveService } from '../../../core/services/leave.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-apply-leave',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './apply-leave.component.html',
  styleUrl: './apply-leave.component.css'
})
export class ApplyLeaveComponent implements OnInit {
  leaveTypes = signal<any[]>([]);
  isLoading = signal(false);
  successMsg = signal('');
  errorMsg = signal('');

  leaveData = {
    leaveTypeId: null as number | null,
    fromDate: '',
    toDate: '',
    reason: ''
  };

  constructor(
    private leaveService: LeaveService,
    private toast: ToastService,
    public auth: AuthService,
    public router: Router
  ) {}

  ngOnInit() {
    console.log('Apply Leave component initialized');
    this.leaveService.getLeaveTypes().subscribe({
      next: (types: any) => {
        console.log('Leave types loaded:', types);
        this.leaveTypes.set(Array.isArray(types) ? types : []);
      },
      error: (err) => {
        console.error('Error loading leave types:', err);
        this.toast.error('Failed to load leave types');
      }
    });
  }

  onSubmit() {
    if (!this.leaveData.leaveTypeId || !this.leaveData.fromDate || !this.leaveData.toDate || !this.leaveData.reason) {
      this.toast.error('Please fill all fields');
      return;
    }

    if (this.isLoading()) return;

    this.isLoading.set(true);
    this.errorMsg.set('');
    this.successMsg.set('');

    const userId = this.auth.getUserId();
    console.log('Submitting leave application for user:', userId);

    this.leaveService.applyLeave({
      userId,
      leaveTypeId: this.leaveData.leaveTypeId!,
      startDate: this.leaveData.fromDate,
      endDate: this.leaveData.toDate,
      reason: this.leaveData.reason
    }).subscribe({
      next: (res) => {
        console.log('Leave applied successfully:', res);
        this.isLoading.set(false);
        this.toast.success('Leave applied successfully!');
        setTimeout(() => this.router.navigate(['/' + this.getRole().toLowerCase() + '/my-leaves']), 1500);
      },
      error: (err) => {
        console.error('Error applying leave:', err);
        this.isLoading.set(false);
        this.toast.error(err.error?.message || 'Failed to apply leave');
      }
    });
  }

  getInitials(name: string | undefined): string {
    if (!name) return '?';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }

  getRole(): 'EMPLOYEE' | 'MANAGER' | 'ADMIN' {
    const role = this.auth.getRole();
    return (role === 'EMPLOYEE' || role === 'MANAGER' || role === 'ADMIN') ? role : 'EMPLOYEE';
  }

  isSidebarOpen = false;
  toggleSidebar() { this.isSidebarOpen = !this.isSidebarOpen; }
}
